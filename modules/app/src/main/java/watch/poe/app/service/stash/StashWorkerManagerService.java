package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.service.ChangeIdService;

import java.util.LinkedList;
import java.util.concurrent.Future;

@Slf4j
@Service
public class StashWorkerManagerService {

    @Autowired
    private StashWorkerService stashWorkerService;

    @Autowired
    private StashWorkerJobSchedulerService jobSchedulerService;

    @Autowired
    private ChangeIdService changeIdService;

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
        var result = stashWorkerService.queryNext();
        workerResultQueue.push(result);
    }

    public void updateChangeId() {
        if (jobSchedulerService.isJobEmpty()) {
            return;
        }

        var jobChangeId = jobSchedulerService.peekJob();
        var repoChangeId = changeIdService.get(ChangeIdService.RIVER);

        if (repoChangeId.isEmpty()) {
            changeIdService.save(ChangeIdService.RIVER, jobChangeId);
        } else if (ChangeIdUtility.isNewerThan(jobChangeId, repoChangeId.get().getChangeId())) {
            changeIdService.update(ChangeIdService.RIVER, jobChangeId);
        }
    }

}
