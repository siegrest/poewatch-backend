package watch.poe.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.stats.mapper.StatMapper;
import watch.poe.stats.model.StatCollector;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.repository.StatCollectorRepository;
import watch.poe.stats.repository.StatHistoryRepository;
import watch.poe.stats.utility.StatsUtility;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatCollectorService {

  private final StatCollectorRepository statCollectorRepository;
  private final StatHistoryRepository statHistoryRepository;
  private final Set<StatCollector> collectors = new HashSet<>();

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onReady() {
    collectors.addAll(statCollectorRepository.findAll());
    log.info("Loaded statistics collectors");
    sync();
  }

  @Scheduled(cron = "${stats.sync.cron}")
  void sync() {
    log.info("Starting stats sync");

    collectors.stream()
      .filter(StatsUtility::isExpired)
      .filter(StatsUtility::hasValues)
      .forEach(c -> {
        var historyEntry = StatMapper.toStatHistory(c);
        statHistoryRepository.save(historyEntry);
        reset(c);
      });

    statCollectorRepository.saveAll(collectors);
    log.info("Finished stats sync");
  }

  private void reset(StatCollector collector) {
    long timeslot = (System.currentTimeMillis() / collector.getTimespan()) * collector.getTimespan();
    LocalDateTime nextPeriod = Instant.ofEpochMilli(timeslot)
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime();

    collector.setStart(nextPeriod);
    collector.setCount(0L);
    collector.setSum(0L);
  }

  private Optional<StatCollector> getCollector(StatType type) {
    return collectors.stream()
      .filter(t -> t.getType() == type)
      .findFirst();
  }

  public void addValue(StatType type, long val) {
    getCollector(type).ifPresentOrElse(c -> {
      c.setCount(c.getCount() + 1);
      c.setSum(c.getSum() + val);
    }, () -> log.error("Stat collector of type '{}' doesn't exists", type));
  }

}
