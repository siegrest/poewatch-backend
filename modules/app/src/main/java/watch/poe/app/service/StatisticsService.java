package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.statistics.StatCollector;
import watch.poe.app.domain.statistics.StatTimer;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.statistics.ThreadTimer;
import watch.poe.app.service.repository.StatisticsRepositoryService;

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

  @EventListener(ApplicationReadyEvent.class)
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
        var threadTimer = getOrCreateThreadTimer();
        var statTimers = threadTimer.getTimers();

        if (statTimers.stream().anyMatch(i -> i.getType().equals(type))) {
            log.error("Stat timer of type '{}' already exists", type);
            throw new RuntimeException();
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
        var threadTimer = getCurrentThreadTimer().orElse(null);
        if (threadTimer == null) {
            log.error("Thread doesn't exist in current context");
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
