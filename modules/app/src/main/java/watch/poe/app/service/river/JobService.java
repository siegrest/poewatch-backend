package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

  @Value("${stash.fetch.cooldown}")
  private int fetchCooldownMs;
  private LocalDateTime lastPollTime;
  private String nextJob;

  @PostConstruct
  public void init() {
    changeIdService.findById(ChangeIdType.APP).ifPresent(id -> {
      lastPollTime = id.getTime();
      nextJob = id.getValue();
    });
  }

  public Optional<String> getNextJob() {
    if (isRequestCooldown()) {
      return Optional.empty();
    }

    lastPollTime = LocalDateTime.now();
    var returnJob = Optional.ofNullable(nextJob);
    nextJob = null;

    return returnJob;
  }

  public void setNextJob(String newJob) {
    if (!ChangeIdUtility.isChangeId(newJob)) {
      throw new RuntimeException("Invalid job provided");
    }

    if (nextJob != null) {
      if (ChangeIdUtility.isNewerThan(nextJob, newJob)) {
        log.warn("Attempted to overwrite job {} with {}", nextJob, newJob);
        return;
      }

      log.warn("Job {} was overwritten by {}", nextJob, newJob);
    }

    changeIdService.saveIfNewer(ChangeIdType.TOP, newJob);
    nextJob = newJob;
  }

  public void setCompletedJob(String completedJob) {
    changeIdService.saveIfNewer(ChangeIdType.TOP, completedJob);
  }

  public boolean isJobEmpty() {
    return nextJob == null;
  }

  public boolean isRequestCooldown() {
    return LocalDateTime.now().isBefore(DateTimeUtility.addMs(lastPollTime, fetchCooldownMs));
  }

}
