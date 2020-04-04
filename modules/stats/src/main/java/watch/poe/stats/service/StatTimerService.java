package watch.poe.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.stats.dto.StatTimerDto;
import watch.poe.stats.model.code.StatType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatTimerService {

  private final StatCollectorService statCollectorService;
  private final Set<StatTimerDto> timers = Collections.synchronizedSet(new HashSet<>());

  private Optional<StatTimerDto> getTimer(StatType type) {
    return timers.stream()
      .filter(t -> t.getType() == type && t.getThread().equals(Thread.currentThread()))
      .findFirst();
  }

  public void startTimer(StatType type) {
    startTimer(type, true, true);
  }

  public void startTimer(StatType type, boolean overwrite, boolean verbose) {
    Optional<StatTimerDto> timer = getTimer(type);

    if (timer.isPresent()) {
      if (verbose) {
        log.error("Stat timer of type '{}' already exists in thread {}", type, Thread.currentThread().getName());
      }
      if (!overwrite) {
        return;
      }
    }

    var threadTimer = StatTimerDto.builder()
      .thread(Thread.currentThread())
      .startTime(LocalDateTime.now())
      .type(type)
      .build();
    timers.add(threadTimer);
  }

  public void clkTimer(StatType type) {
    clkTimer(type, false);
  }

  public void clkTimer(StatType type, boolean verbose) {
    Optional<StatTimerDto> timer = getTimer(type);
    if (timer.isEmpty()) {
      if (verbose) {
        log.error("Stat timer of type '{}' doesn't exists in thread {}", type, Thread.currentThread().getName());
      }
      return;
    }

    timers.remove(timer.get());

    long delay = ChronoUnit.MILLIS.between(timer.get().getStartTime(), LocalDateTime.now());
    statCollectorService.addValue(type, delay);

    if (verbose) {
      log.info("Stat timer of type '{}' completed in {}ms", timer.get().getType(), delay);
    }
  }

  public void addValue(StatType type) {
    statCollectorService.addValue(type, 0);
  }

  public void addValue(StatType type, long value) {
    statCollectorService.addValue(type, value);
  }

}
