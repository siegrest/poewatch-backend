package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.config.AppModuleConfig;
import watch.poe.app.service.ChangeIdService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.app.utility.DateTimeUtility;
import watch.poe.persistence.model.changeId.ChangeIdType;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

  private final ChangeIdService changeIdService;
  private final AppModuleConfig config;

  @Value("${stash.fetch.cooldown}")
  private int fetchCooldown;

  // todo: query job from repository
  private String job;
  private LocalDateTime lastPollTime;

  @PostConstruct
  public void init() {
    var changeIdOverride = config.getProperty("develop.change-id.override");
    if (ChangeIdUtility.isChangeId(changeIdOverride)) {
      job = changeIdOverride;
    } else {
      changeIdService.find(ChangeIdType.APP).ifPresent(id -> job = id.getValue());
    }
  }

  public Optional<String> getJob() {
    if (isRequestCooldown()) {
      return Optional.empty();
    }

    lastPollTime = LocalDateTime.now();
    var returnJob = Optional.ofNullable(job);
    job = null;

    return returnJob;
  }

  public void setJob(String newJob) {
    if (!ChangeIdUtility.isChangeId(newJob)) {
      throw new RuntimeException("Invalid job provided");
    }

    if (job != null) {
      if (ChangeIdUtility.isNewerThan(job, newJob)) {
        log.warn("Attempted to overwrite job {} with {}", job, newJob);
        return;
      }

      log.warn("Job {} was overwritten by {}", job, newJob);
    }

    changeIdService.saveIfNewer(ChangeIdType.TOP, newJob);
    job = newJob;
  }

  public boolean isJobEmpty() {
    return job == null;
  }

  public boolean isRequestCooldown() {
    return LocalDateTime.now().isBefore(DateTimeUtility.addMs(lastPollTime, fetchCooldown));
  }

}
