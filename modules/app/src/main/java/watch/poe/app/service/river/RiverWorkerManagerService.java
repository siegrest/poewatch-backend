package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.wrapper.RiverWrapper;
import watch.poe.app.exception.RiverDownloadException;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.service.StatTimerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverWorkerManagerService {

  private final RiverWorkerService riverWorkerService;
  private final FutureHandlerService futureHandlerService;
  private final StatTimerService statTimerService;
  private final JobService jobService;

  @Value("${stash.worker.count}")
  private int maxWorkerCount;
  @Value("${stash.parse.batch.size}")
  private int batchSize;

  private Set<Future<RiverWrapper>> riverFutures = new HashSet<>();
  private Future<String> indexFuture;

  @Scheduled(fixedRateString = "${stash.worker.query.rate}", initialDelay = 2000)
  public void scheduleWorker() {
    if (riverFutures.isEmpty() && jobService.isJobEmpty()) {
      // todo: custom exception
      throw new RuntimeException("No active workers and next change id is empty");
    }

    if (riverFutures.size() >= maxWorkerCount) {
      statTimerService.startTimer(StatType.TIME_WORKERS_IDLE, false, false);
      return;
    }

    statTimerService.clkTimer(StatType.TIME_WORKERS_IDLE, false);

    jobService.getNextJob().ifPresent(job -> {
      Future<RiverWrapper> riverWrapperFuture = riverWorkerService.queryNext(job);
      riverFutures.add(riverWrapperFuture);
    });
  }

  @Scheduled(fixedRateString = "${stash.worker.check.rate}")
  public void checkFinishedRiverFuture() throws InterruptedException, ExecutionException {
    if (indexFuture != null) {
      if (!indexFuture.isDone()) {
        return;
      }

      // todo: handle index exceptions
      jobService.setCompletedJob(indexFuture.get());
      indexFuture = null;
    }

    var completedFutures = riverFutures.stream()
      .filter(Future::isDone)
      .collect(Collectors.toList());

    if (completedFutures.isEmpty()) {
      return;
    }

    if (completedFutures.size() < batchSize) {
      return;
    }

    riverFutures.removeAll(completedFutures);

    List<RiverWrapper> wrappers = new ArrayList<>();
    for (Future<RiverWrapper> completedFuture : completedFutures) {
      try {
        var wrapper = completedFuture.get();
        wrappers.add(wrapper);
      } catch (RiverDownloadException ex) {
        // todo: handle exceptions - redo parameterized download
        log.error("Caught river worker exception", ex);
      }
    }

    indexFuture = futureHandlerService.process(wrappers);
  }

  @Scheduled(fixedRate = 1000)
  public void debug() {
    var queueSize = riverFutures.size();
    var finished = riverFutures.stream().filter(Future::isDone).count();
    var unfinished = riverFutures.stream().filter(Future::isDone).count();

    log.info("queue size {}: finished/pending {}, in progress {}", queueSize, finished, unfinished);
  }

}
