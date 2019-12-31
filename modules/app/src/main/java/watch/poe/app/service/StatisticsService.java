package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.*;
import watch.poe.app.service.repository.StatisticsRepositoryService;
import watch.poe.persistence.model.StatisticPartial;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private StatisticsRepositoryService statisticsRepositoryService;

    private final ConcurrentHashMap<Thread, Set<StatTimer>> threadTimers = new ConcurrentHashMap<>();

    private final List<StatCollector> collectors = Collections.synchronizedList(List.of(
            new StatCollector(StatType.TIME_API_REPLY_DOWNLOAD, StatGroupType.AVG, TimeFrame.M_60),
            new StatCollector(StatType.TIME_REPLY_PARSE, StatGroupType.AVG, TimeFrame.M_60),
            new StatCollector(StatType.TIME_API_TTFB, StatGroupType.AVG, TimeFrame.M_60),

            new StatCollector(StatType.COUNT_API_ERRORS_DUPLICATE, StatGroupType.SUM, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_ERRORS_CONNECT_TIMEOUT, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_ERRORS_READ_TIMEOUT, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_ERRORS_CONN_RESET, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_ERRORS_5XX, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_ERRORS_4XX, StatGroupType.COUNT, TimeFrame.M_60),

            new StatCollector(StatType.COUNT_REPLY_SIZE, StatGroupType.AVG, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_API_CALLS, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_TOTAL_STASHES, StatGroupType.SUM, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_TOTAL_ITEMS, StatGroupType.SUM, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_ACCEPTED_ITEMS, StatGroupType.SUM, TimeFrame.M_60),
            new StatCollector(StatType.COUNT_ACTIVE_ACCOUNTS, StatGroupType.SUM, TimeFrame.M_60),

            new StatCollector(StatType.MISC_APP_STARTUP, StatGroupType.COUNT, TimeFrame.M_60),
            new StatCollector(StatType.MISC_APP_SHUTDOWN, StatGroupType.COUNT, TimeFrame.M_60)
    ));

    @Scheduled(initialDelayString = "${stats.sync.delay.initial}", fixedDelay = Long.MAX_VALUE)
    public void onReady() {
        // Get ongoing statistics collectors from database
        var pStats = statisticsRepositoryService.getPartialStatistics();
        fillCollectors(pStats);

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

    private void fillCollectors(List<StatisticPartial> partialStatistics) {
        partialStatistics.forEach(ps -> {
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
    }

    @Scheduled(cron = "${stats.sync.cron}")
    public void sync() {
        log.info("Stating stats sync");

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
        var statTimerList = threadTimers.getOrDefault(Thread.currentThread(), new HashSet<>());
        threadTimers.putIfAbsent(Thread.currentThread(), statTimerList);

        // If there was no entry, create a new one and add it to the list
        if (statTimerList.stream().anyMatch(i -> i.getType().equals(type))) {
            log.error("Stat timer of type '{}' already exists", type);
            throw new RuntimeException();
        }

        statTimerList.add(new StatTimer(type));
    }

    public void clkTimer(StatType type) {
        var statEntryList = threadTimers.get(Thread.currentThread());
        if (statEntryList == null) {
            log.error("Thread doesn't exist in current context");
            return;
        }

        // Find first timer
        var statTimer = statEntryList.stream()
                .filter(i -> i.getType().equals(type))
                .findFirst()
                .orElse(null);

        // If it didn't exist
        if (statTimer == null) {
            return;
        }

        // Remove the timer
        statEntryList.remove(statTimer);

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
