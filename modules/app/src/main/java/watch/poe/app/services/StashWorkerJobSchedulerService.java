package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.config.AppModuleConfig;
import watch.poe.app.utility.ChangeIdUtility;

@Slf4j
@Service
public class StashWorkerJobSchedulerService {

    @Autowired
    private AppModuleConfig config;

    private String job = "0-0-0-0-0";
    private long lastPollTime = 0;

    public String getJob() {
        if (job == null) {
            log.error("Empty change id returned");
        }

        String returnJob = job;
        job = null;
        return returnJob;
    }

    public void setJob(String newJob) {
        if (!ChangeIdUtility.isChangeId(newJob)) {
            log.error("Invalid change id provided: {}", newJob);
            return;
        }

        if (job != null) {
            log.error("Change id {} was overwritten by {}", job, newJob);
        }

        log.info("found new job {}", newJob); // todo: remove me
        job = newJob;
    }

    public boolean isJobEmpty() {
        return job == null;
    }

    public void bumpPollTime() {
        lastPollTime = System.currentTimeMillis();
    }

    public boolean isCooldown() {
        return System.currentTimeMillis() - lastPollTime < config.getPropertyAsInt("stash.fetch.rate");
    }

}
