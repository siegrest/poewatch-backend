package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.StatType;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.app.utility.HttpUtility;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

@Slf4j
@Service
public class StashWorkerService {

    @Autowired
    private StashWorkerJobSchedulerService jobSchedulerService;
    @Autowired
    private StashParserService stashParserService;
    @Autowired
    private StatisticsService ss;

    @Value("${stash.fetch.url}")
    private String endpointUrl;
    @Value("${stash.fetch.timeout.connect}")
    private int connectTimeOut;
    @Value("${stash.fetch.timeout.read}")
    private int readTimeOut;

    @Async
    public Future<String> queryNext() {
        var nextChangeId = jobSchedulerService.getJob();

        ss.addValue(StatType.COUNT_API_CALLS);
        log.debug("Started worker with job {}", nextChangeId);

        var stashJsonString = downloadStashJson(nextChangeId);

        ss.startTimer(StatType.TIME_PARSE_REPLY);
        stashParserService.parse(stashJsonString);
        ss.clkTimer(StatType.TIME_PARSE_REPLY);

        return new AsyncResult<>("worker finished");
    }

    private StringBuilder downloadStashJson(String changeId) {
        StringBuilder streamResult = null;
        InputStream stream = null;

        try {
            ss.startTimer(StatType.TIME_API_REPLY_DOWNLOAD);

            URL request = new URL(endpointUrl + "?id=" + changeId);
            HttpURLConnection connection = (HttpURLConnection) request.openConnection();
            connection.setReadTimeout(readTimeOut);
            connection.setConnectTimeout(connectTimeOut);

            stream = connection.getInputStream();
            streamResult = streamStashes(stream);

        } catch (MalformedURLException ex) {

            log.error("Failed to build stash api url", ex);

        } catch (IOException ex) {

            log.error("Caught stash api worker exception", ex);
            var statType = HttpUtility.getErrorType(ex);
            if (statType != null) {
                ss.addValue(statType);
            }

        } finally {
            ss.clkTimer(StatType.TIME_API_REPLY_DOWNLOAD);

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

        ss.startTimer(StatType.TIME_API_TTFB);

        while ((byteCount = stream.read(byteBuffer, 0, 128)) != -1) {
            if (!gotFirstByte) {
                ss.clkTimer(StatType.TIME_API_TTFB);
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

        ss.addValue(StatType.COUNT_REPLY_SIZE, totalByteCount);
        return jsonBuffer;
    }

}
