package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.concurrent.Future;

@Slf4j
@Service
public class StashWorkerManagerService {

    @Autowired
    private StashWorkerService stashWorkerService;

    @Autowired
    private StashWorkerJobSchedulerService jobSchedulerService;

    @Value("${stash.worker.count}")
    private int maxWorkerCount;

    private LinkedList<Future<String>> workerResultQueue = new LinkedList<>();

    @Scheduled(fixedRate = 100)
    public void scheduleWorker() {
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

        jobSchedulerService.bumpPollTime();
        var result = stashWorkerService.queryNext();
        workerResultQueue.push(result);
    }

}
