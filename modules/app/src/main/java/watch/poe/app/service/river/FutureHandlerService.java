package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.item.ItemIndexerService;
import watch.poe.app.service.repository.LeagueItemEntryService;
import watch.poe.persistence.model.LeagueItemEntry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutureHandlerService {

  private final StatisticsService statisticsService;
  private final ItemIndexerService itemIndexerService;
  private final LeagueItemEntryService itemEntryService;

  @Async
  @Transactional
  public Future<Boolean> process(RiverWrapper wrapper) {
    log.info("Starting index job");

    statisticsService.startTimer(StatType.TIME_REPLY_INDEX);
    for (LeagueItemEntry entry : wrapper.getEntries()) {
      // todo: filter out duplicates
      var item = itemIndexerService.index(entry.getItem());
      entry.setItem(item);
      // todo: save entries in batch
      itemEntryService.save(entry);
    }
    statisticsService.clkTimer(StatType.TIME_REPLY_INDEX, true);

    return CompletableFuture.completedFuture(true);
  }

}
