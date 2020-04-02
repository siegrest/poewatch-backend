package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.statistics.StatType;
import watch.poe.app.dto.wrapper.RiverWrapper;
import watch.poe.app.exception.river.RiverDownloadBasis;
import watch.poe.app.exception.river.RiverDownloadException;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.app.utility.StatsUtility;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverWorkerService {

  private final JobService jobSchedulerService;
  private final RiverParserService riverParserService;
  private final StatisticsService statisticsService;

  @Value("${stash.fetch.url}")
  private String endpointUrl;
  @Value("${stash.fetch.timeout.connect}")
  private int connectTimeOut;
  @Value("${stash.fetch.timeout.read}")
  private int readTimeOut;

  @Async
  public Future<RiverWrapper> queryNext(String job) throws RiverDownloadException {
    log.info("Started worker with job {}", job);
    statisticsService.addValue(StatType.COUNT_API_CALLS);

    var stashStringBuilder = downloadStashJson(job);
    if (stashStringBuilder == null) {
      return AsyncResult.forExecutionException(new RiverDownloadException(RiverDownloadBasis.UNKNOWN));
    }

    return riverParserService.process(job, stashStringBuilder);
  }

  private StringBuilder downloadStashJson(String changeId) {
    statisticsService.startTimer(StatType.TIME_API_REPLY_DOWNLOAD);

    StringBuilder streamResult = null;
    InputStream stream = null;

    try {
      URL request = new URL(endpointUrl + "?id=" + changeId);
      HttpURLConnection connection = (HttpURLConnection) request.openConnection();
      connection.setReadTimeout(readTimeOut);
      connection.setConnectTimeout(connectTimeOut);

      statisticsService.startTimer(StatType.TIME_API_TTFB);

      stream = connection.getInputStream();
      streamResult = streamStashes(stream);

    } catch (MalformedURLException ex) {

      log.error("Failed to build stash api url", ex);

    } catch (IOException ex) {

      jobSchedulerService.setJob(changeId);

      var exception = new RiverDownloadException(ex);
      var statType = StatsUtility.getErrorType(exception.getBasis());
      if (statType != null) {
        statisticsService.addValue(statType);
      }

      throw exception;

    } finally {
      statisticsService.clkTimer(StatType.TIME_API_REPLY_DOWNLOAD);
      // precaution if the stream method finished abruptly
      statisticsService.clkTimer(StatType.TIME_API_TTFB);

      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException ex) {
        log.error("Could not close stream", ex);
      }
    }

    return streamResult;
  }

  private StringBuilder streamStashes(InputStream stream) throws IOException {
    StringBuilder jsonBuffer = new StringBuilder();
    boolean isCheckNextChangeId = true;
    boolean gotFirstByte = false;

    int byteCount, totalByteCount = 0;
    byte[] byteBuffer = new byte[128];

    while ((byteCount = stream.read(byteBuffer, 0, 128)) != -1) {
      if (!gotFirstByte) {
        statisticsService.clkTimer(StatType.TIME_API_TTFB);
        gotFirstByte = true;
      }

      totalByteCount += byteCount;

      // Check if byte has <CHUNK_SIZE> amount of elements (the first request does not)
      if (byteCount != 128) {
        byte[] trimmedByteBuffer = new byte[byteCount];
        System.arraycopy(byteBuffer, 0, trimmedByteBuffer, 0, byteCount);
        // Trim byteBuffer, convert it into string and add to string buffer
        jsonBuffer.append(new String(trimmedByteBuffer));
      } else {
        jsonBuffer.append(new String(byteBuffer));
      }

      // attempt the find next change id from current buffer. since it's the first property of the reply,
      // this won't create that big of an overhead hopefully
      if (isCheckNextChangeId) {
        String nextChangeId = ChangeIdUtility.find(jsonBuffer.toString());
        if (nextChangeId != null) {
          isCheckNextChangeId = false;
          jobSchedulerService.setJob(nextChangeId);
        }
      }
    }

    statisticsService.addValue(StatType.COUNT_REPLY_SIZE, totalByteCount);
    return jsonBuffer;
  }

}
