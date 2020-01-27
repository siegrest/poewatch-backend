package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.item.ItemIndexerService;
import watch.poe.app.service.repository.ChangeIdRepositoryService;
import watch.poe.app.utility.ChangeIdUtility;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverWorkerManagerService {

  private final RiverWorkerService riverWorkerService;
  private final RiverWorkerJobSchedulerService jobSchedulerService;
  private final ChangeIdRepositoryService changeIdRepositoryService;
  private final StatisticsService statisticsService;
  private final ItemIndexerService itemIndexerService;

  @Value("${stash.worker.count}")
  private int maxWorkerCount;
  @Value("${stash.fetch.enabled}")
  private boolean enabled;

  private Set<Future<RiverWrapper>> riverFutures = new HashSet<>();
  private Future<Boolean> indexFuture;

  @Scheduled(fixedRateString = "${stash.worker.query.rate}")
  public void scheduleWorker() throws InterruptedException, ExecutionException {
    if (!enabled) {
      return;
    }

    checkFinishedRiverFuture();

    if (riverFutures.isEmpty() && jobSchedulerService.isJobEmpty()) {
      throw new RuntimeException("No active workers and next change id is empty");
    }

    if (riverFutures.size() >= maxWorkerCount) {
      return;
    }

    var nextJob = jobSchedulerService.getJob();
    if (nextJob.isEmpty()) {
      return;
    }

    updateRepoJob(nextJob.get());
    riverFutures.add(riverWorkerService.queryNext(nextJob.get()));
    statisticsService.addValue(StatType.COUNT_API_CALLS);
  }

  private void checkFinishedRiverFuture() throws InterruptedException, ExecutionException {
    if (indexFuture != null) {
      if (indexFuture.isDone() || indexFuture.isCancelled()) {
        indexFuture = null;
      } else {
        return;
      }
    }

    var iterator = riverFutures.iterator();
    while (iterator.hasNext()) {
      var riverFuture = iterator.next();
      if (!riverFuture.isDone()) {
        continue;
      }

      var wrapper = riverFuture.get();
      log.info("Starting index job");
      // todo: move entry saving to another service
      // todo: save entries in batch
      indexFuture = itemIndexerService.startIndexJob(wrapper);
      iterator.remove();
    }
  }

  @Scheduled(fixedRate = 1000)
  public void debug() {
    var queueSize = riverFutures.size();
    var finished = riverFutures.stream().filter(r -> r.isDone() || r.isCancelled()).count();
    var unfinished = riverFutures.stream().filter(r -> !r.isDone() && !r.isCancelled()).count();

    log.info("queue size {}: finished/pending {}, in progress {}", queueSize, finished, unfinished);
  }

  private void updateRepoJob(String nextJob) {
    var repoJob = changeIdRepositoryService.get(ChangeIdRepositoryService.RIVER);

    if (repoJob.isEmpty()) {
      changeIdRepositoryService.save(ChangeIdRepositoryService.RIVER, nextJob);
    } else if (ChangeIdUtility.isNewerThan(nextJob, repoJob.get().getChangeId())) {
      changeIdRepositoryService.update(ChangeIdRepositoryService.RIVER, nextJob);
    }
  }

}
