package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.config.AppModuleConfig;

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
    private AppModuleConfig config;

    private LinkedList<Future<String>> workerResultQueue = new LinkedList<>();

    @Scheduled(fixedRate = 1000)
    public void scheduleWorker() {
        if (workerResultQueue.isEmpty() && jobSchedulerService.isJobEmpty()) {
            throw new RuntimeException("No active workers and next change id is null");
        }

        if (hasCompletedWorkers()) {
            log.info("found completed workers. processing...");
            workerResultQueue.removeIf(Future::isDone);
        }

        // no available job
        if (jobSchedulerService.isJobEmpty()) {
            return;
        }

        // worker limit reached
        if (workerResultQueue.size() >= config.getPropertyAsInt("stash.worker.count")) {
            log.info("worker limit reached");
            return;
        }

        if (jobSchedulerService.isCooldown()) {
            log.info("poll limit reached");
            return;
        }

        jobSchedulerService.bumpPollTime();
        var result = stashWorkerService.queryNext();
        workerResultQueue.push(result);
    }

    private boolean hasCompletedWorkers() {
        for (var workerResult : workerResultQueue) {
            if (workerResult.isDone()) {
                return true;
            }
        }

        return false;
    }

}
