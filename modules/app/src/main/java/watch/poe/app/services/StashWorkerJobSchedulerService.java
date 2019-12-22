package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;

@Slf4j
@Service
public class StashWorkerJobSchedulerService {

    @Value("${stash.fetch.rate}")
    private int fetchRate;

    // todo: query job from repository
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

        job = newJob;
    }

    public boolean isJobEmpty() {
        return job == null;
    }

    public void bumpPollTime() {
        lastPollTime = System.currentTimeMillis();
    }

    public boolean isCooldown() {
        return System.currentTimeMillis() - lastPollTime < fetchRate;
    }

}
