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
import watch.poe.app.service.repository.LeagueItemEntryService;
import watch.poe.app.utility.ChangeIdUtility;

import java.util.LinkedList;
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
  private final LeagueItemEntryService itemEntryService;

  @Value("${stash.worker.count}")
  private int maxWorkerCount;
  @Value("${stash.fetch.enabled}")
  private boolean enabled;

  private LinkedList<Future<RiverWrapper>> workerResultQueue = new LinkedList<>();

  @Scheduled(fixedRateString = "${stash.worker.query.rate}")
  private void scheduleWorker() {
    if (!enabled) {
      return;
    }

    if (workerResultQueue.isEmpty() && jobSchedulerService.isJobEmpty()) {
      log.error("No active workers and next change id is empty");
      return;
    }

    if (jobSchedulerService.isJobEmpty()) {
      return;
    }

    if (workerResultQueue.size() >= maxWorkerCount) {
      log.debug("Worker limit reached");
      return;
    }

    if (jobSchedulerService.isCooldown()) {
      log.debug("Poll on cooldown");
      return;
    }

    updateChangeId();
    jobSchedulerService.bumpPollTime();
    statisticsService.addValue(StatType.COUNT_API_CALLS);
    var result = riverWorkerService.queryNext();
    workerResultQueue.push(result);
  }

  @Scheduled(fixedRateString = "${stash.worker.check.rate}")
  private void checkFinishedJobs() throws InterruptedException, ExecutionException {
    var iterator = workerResultQueue.iterator();

    while (iterator.hasNext()) {
      var future = iterator.next();
      if (!future.isDone()) {
        continue;
      }

      iterator.remove();
      var wrapper = future.get();

      statisticsService.startTimer(StatType.TIME_REPLY_INDEX);
      wrapper.getEntries().forEach(entry -> {
        var item = itemIndexerService.index(entry.getItem());
        entry.setItem(item);
        itemEntryService.save(entry);
      });
      statisticsService.clkTimer(StatType.TIME_REPLY_INDEX, true);
    }
  }

  private void updateChangeId() {
    if (jobSchedulerService.isJobEmpty()) {
      return;
    }

    var jobChangeId = jobSchedulerService.peekJob();
    var repoChangeId = changeIdRepositoryService.get(ChangeIdRepositoryService.RIVER);

    if (repoChangeId.isEmpty()) {
      changeIdRepositoryService.save(ChangeIdRepositoryService.RIVER, jobChangeId);
    } else if (ChangeIdUtility.isNewerThan(jobChangeId, repoChangeId.get().getChangeId())) {
      changeIdRepositoryService.update(ChangeIdRepositoryService.RIVER, jobChangeId);
    }
  }

}
