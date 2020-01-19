package watch.poe.app.service.river;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;

@Slf4j
@Service
public class RiverWorkerJobSchedulerService {

    @Value("${stash.fetch.cooldown}")
    private int fetchCooldown;

    // todo: query job from repository
//    private String job = "541640378-559363977-529069322-602859172-572858041";
    private String job = "542419420-560211954-529765688-603620935-57359360";
    private long lastPollTime = 0;

    public String peekJob() {
        if (job == null) {
            log.error("Empty change id returned");
        }

        return job;
    }

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
        return System.currentTimeMillis() - lastPollTime < fetchCooldown;
    }

}
