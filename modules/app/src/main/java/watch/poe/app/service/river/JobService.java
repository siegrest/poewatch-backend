package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.service.repository.ChangeIdService;
import watch.poe.app.utility.ChangeIdUtility;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

  private final ChangeIdService changeIdService;

  @Value("${stash.fetch.cooldown}")
  private int fetchCooldown;

//    private String job = "541640378-559363977-529069322-602859172-572858041";
//    private String job = "542419420-560211954-529765688-603620935-57359360";
//    private String job = "544998848-562898891-532480647-606437628-78043036";

  // todo: query job from repository
  private String job = "546007976-563951214-533531225-607571032-78793199";
  private long lastPollTime = System.currentTimeMillis();

  public Optional<String> getJob() {
    if (isRequestCooldown()) {
      return Optional.empty();
    }

    lastPollTime = System.currentTimeMillis();
    var returnJob = Optional.ofNullable(job);
    job = null;

    return returnJob;
  }

  public void setJob(String newJob) {
    if (!ChangeIdUtility.isChangeId(newJob)) {
      throw new RuntimeException("Invalid job provided");
    }

    if (job != null) {
      var age = ChangeIdUtility.isNewerThan(newJob, job) ? "newer" : "older";
      log.error("Job {} was overwritten by {} {}", job, age, newJob);
    }

    changeIdService.saveRiverIfNew(newJob);
    job = newJob;
  }

  public boolean isJobEmpty() {
    return job == null;
  }

  public boolean isRequestCooldown() {
    return System.currentTimeMillis() - lastPollTime < fetchCooldown;
  }

}
