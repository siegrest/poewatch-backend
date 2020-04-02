package watch.poe.app.service.statistic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.statistics.StatCollector;
import watch.poe.app.dto.statistics.StatTimer;
import watch.poe.app.dto.statistics.ThreadTimer;
import watch.poe.persistence.model.statistic.StatType;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final StatisticsRepositoryService statisticsRepositoryService;
  private final Set<StatCollector> collectors;

  private Set<ThreadTimer> threadTimers = Collections.synchronizedSet(new HashSet<>());

  @PostConstruct
  public void onReady() {
    // Get ongoing statistics collectors from database
    statisticsRepositoryService.getPartialStatistics().forEach(ps -> {
      var collector = collectors.stream()
        .filter(c -> c.getType().equals(StatType.valueOf(ps.getType())))
        .findFirst();
      if (collector.isPresent()) {
        collector.get().setCount(ps.getCount());
        collector.get().setSum(ps.getSum());
        collector.get().setCreationTime(ps.getTime());
      } else {
        log.error("Could not find a match for partial statistic type {}", ps.getType());
      }
    });

    log.info("Loaded partial statistics into collectors");

    // Find if any of the collectors have expired during the time the app was offline
    Set<StatCollector> expired = collectors.stream()
      .filter(StatCollector::isRecorded)
      .filter(StatCollector::hasValues)
      .filter(StatCollector::isExpired)
      .collect(Collectors.toSet());

    // Delete and create database entries
    expired.forEach(c -> {
      log.info("Found expired collector during initialization: {}", c.getType());
      statisticsRepositoryService.deletePartialByType(c.getType().name());
      statisticsRepositoryService.saveToHistory(c.getType().name(), c.getInsertTime(), c.getValue());
      c.reset();
    });
  }

  @Scheduled(cron = "${stats.sync.cron}")
  public void sync() {
    log.info("Starting stats sync");

    // Find collectors that are expired
    Set<StatCollector> expired = collectors.stream()
      .filter(StatCollector::isRecorded)
      .filter(StatCollector::hasValues)
      .filter(StatCollector::isExpired)
      .collect(Collectors.toSet());

    // Find collectors that are still ongoing
    Set<StatCollector> unexpired = collectors.stream()
      .filter(StatCollector::isRecorded)
      .filter(StatCollector::hasValues)
      .filter(i -> !i.isExpired())
      .collect(Collectors.toSet());

    expired.forEach(c -> {
      statisticsRepositoryService.saveToHistory(c.getType().name(), c.getInsertTime(), c.getValue());
      statisticsRepositoryService.deletePartialByType(c.getType().name());
    });

    unexpired.forEach(c -> {
      statisticsRepositoryService.saveToPartial(c.getType().name(), c.getInsertTime(), c.getSum(), c.getCount());
    });

    expired.forEach(StatCollector::reset);
    log.debug("Finished stats sync");
  }

  public void startTimer(StatType type) {
    startTimer(type, true, true);
  }

  public void startTimer(StatType type, boolean overwrite, boolean verbose) {
    var statTimers = getOrCreateThreadTimer().getTimers();

    if (statTimers.stream().anyMatch(i -> i.getType().equals(type))) {
      if (verbose) {
        log.error("Stat timer of type '{}' already exists in thread {}", type, Thread.currentThread().getName());
      }
      if (!overwrite) {
        return;
      }
    }

    var timer = StatTimer.builder()
      .startTime(System.currentTimeMillis())
      .type(type)
      .build();
    statTimers.add(timer);
  }

  private ThreadTimer getOrCreateThreadTimer() {
    var oThreadTimer = getCurrentThreadTimer();
    if (oThreadTimer.isPresent()) {
      return oThreadTimer.get();
    }

    var threadTimer = ThreadTimer.builder()
      .thread(Thread.currentThread())
      .timers(new HashSet<>())
      .build();
    threadTimers.add(threadTimer);
    return threadTimer;
  }

  private Optional<ThreadTimer> getCurrentThreadTimer() {
    return threadTimers.stream()
      .filter(t -> t.getThread().equals(Thread.currentThread()))
      .findFirst();
  }

  public void clkTimer(StatType type) {
    clkTimer(type, false);
  }

  public void clkTimer(StatType type, boolean verbose) {
    var threadTimer = getCurrentThreadTimer().orElse(null);
    if (threadTimer == null) {
      if (verbose) {
        log.error("Thread doesn't exist in current context");
      }
      return;
    }

    // Find first timer
    var statTimer = threadTimer.getTimers().stream()
      .filter(i -> i.getType().equals(type))
      .findFirst()
      .orElse(null);

    // If it didn't exist
    if (statTimer == null) {
      return;
    }

    // Remove the timer
    threadTimer.getTimers().remove(statTimer);

    // Get delay as MS
    var delay = System.currentTimeMillis() - statTimer.getStartTime();
    addValue(type, delay);

    if (verbose) {
      log.info("Timer '{}' completed in {}ms", statTimer.getType(), delay);
    }
  }

  public void addValue(StatType type, int val) {
    addValue(type, Long.valueOf(val));
  }

  public void addValue(StatType type, Long val) {
    var collector = collectors.stream()
      .filter(i -> i.getType().equals(type))
      .findFirst()
      .orElse(null);

    if (collector == null) {
      log.error("The collector {} could not be found", type);
      return;
    }

    collector.addValue(val);
  }

  public void addValue(StatType type) {
    addValue(type, null);
  }

}
