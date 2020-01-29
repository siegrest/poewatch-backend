package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.chid.ChangeIdService;
import watch.poe.persistence.domain.ChangeIdId;

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
  private final ChangeIdService changeIdService;
  private final FutureHandlerService futureHandlerService;
  private final JobService jobService;

  @Value("${stash.worker.count}")
  private int maxWorkerCount;
  @Value("${stash.fetch.enabled}")
  private boolean enabled;

  private Set<Future<RiverWrapper>> riverFutures = new HashSet<>();
  private Future<String> indexFuture;

  @Scheduled(fixedRateString = "${stash.worker.query.rate}", initialDelay = 2000)
  public void scheduleWorker() {
    if (!enabled) {
      return;
    }

    if (riverFutures.isEmpty() && jobService.isJobEmpty()) {
      throw new RuntimeException("No active workers and next change id is empty");
    }

    if (riverFutures.size() >= maxWorkerCount) {
      return;
    }

    var nextJob = jobService.getJob();
    if (nextJob.isEmpty()) {
      return;
    }

    riverFutures.add(riverWorkerService.queryNext(nextJob.get()));
  }

  @Scheduled(fixedRateString = "${stash.worker.check.rate}")
  public void checkFinishedRiverFuture() throws InterruptedException, ExecutionException {
    if (indexFuture != null) {
      if (!indexFuture.isDone() && !indexFuture.isCancelled()) {
        return;
      }

      // todo: handle index exceptions
      var completedJob = indexFuture.get();
      changeIdService.saveIfNewer(ChangeIdId.APP, completedJob);
      indexFuture = null;
    }

    var completedFutures = riverFutures.stream()
      .filter(rf -> rf.isDone() || rf.isCancelled())
      .collect(Collectors.toList());

    if (completedFutures.isEmpty()) {
      return;
    }

    riverFutures.removeAll(completedFutures);

    List<RiverWrapper> wrappers = new ArrayList<>();
    for (Future<RiverWrapper> completedFuture : completedFutures) {
      // todo: handle exceptions
      var wrapper = completedFuture.get();
      wrappers.add(wrapper);
    }

    indexFuture = futureHandlerService.process(wrappers);
  }

  @Scheduled(fixedRate = 1000)
  public void debug() {
    var queueSize = riverFutures.size();
    var finished = riverFutures.stream().filter(r -> r.isDone() || r.isCancelled()).count();
    var unfinished = riverFutures.stream().filter(r -> !r.isDone() && !r.isCancelled()).count();

    log.info("queue size {}: finished/pending {}, in progress {}", queueSize, finished, unfinished);
  }

}
