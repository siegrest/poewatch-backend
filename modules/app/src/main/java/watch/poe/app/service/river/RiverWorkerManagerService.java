package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.repository.ChangeIdRepositoryService;
import watch.poe.app.utility.ChangeIdUtility;

import java.util.LinkedList;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverWorkerManagerService {

  private final RiverWorkerService riverWorkerService;
  private final RiverWorkerJobSchedulerService jobSchedulerService;
  private final ChangeIdRepositoryService changeIdRepositoryService;
  private final StatisticsService statisticsService;

  @Value("${stash.worker.count}")
  private int maxWorkerCount;
  @Value("${stash.fetch.enabled}")
  private boolean enabled;

  private LinkedList<Future<String>> workerResultQueue = new LinkedList<>();

  @Scheduled(fixedRateString = "${stash.worker.rate}")
  public void scheduleWorker() {
        if (!enabled) {
            return;
        }

        workerResultQueue.removeIf(Future::isDone);

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

    public void updateChangeId() {
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
