package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.cache.CategoryCacheService;
import watch.poe.app.service.cache.GroupCacheService;
import watch.poe.app.service.cache.ItemBaseCacheService;
import watch.poe.app.service.cache.ItemCacheService;
import watch.poe.app.service.repository.LeagueItemEntryService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.LeagueItemEntry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutureHandlerService {

  private final StatisticsService statisticsService;
  private final LeagueItemEntryService itemEntryService;

  private final CategoryCacheService categoryCacheService;
  private final GroupCacheService groupCacheService;
  private final ItemBaseCacheService itemBaseCacheService;
  private final ItemCacheService itemCacheService;

  @Async
  @Transactional
  public Future<String> process(List<RiverWrapper> wrappers) {
    var entries = wrappers.stream()
      .map(RiverWrapper::getEntries)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    log.info("Starting index job with {} entries", entries.size());

    statisticsService.startTimer(StatType.TIME_REPLY_INDEX);
    for (LeagueItemEntry entry : entries) {
      // todo: filter out duplicates
      entry.setItem(index(entry.getItem()));
    }
    statisticsService.clkTimer(StatType.TIME_REPLY_INDEX, true);

    statisticsService.startTimer(StatType.TIME_REPLY_PERSIST);
    itemEntryService.saveAll(entries);
    statisticsService.clkTimer(StatType.TIME_REPLY_PERSIST, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);

    return CompletableFuture.completedFuture(newestJob);
  }

  private Item index(Item item) {
    var base = item.getBase();

    var category = categoryCacheService.get(base.getCategory().getName());
    if (category.isEmpty()) {
      // todo: custom exception
      throw new RuntimeException(String.format("Expected to find category '%s'", base.getCategory().getName()));
    } else {
      base.setCategory(category.get());
    }

    var group = groupCacheService.get(base.getGroup().getName());
    if (group.isEmpty()) {
      // todo: custom exception
      throw new RuntimeException(String.format("Expected to find group '%s'", base.getCategory().getName()));
    } else {
      base.setGroup(group.get());
    }

    var itemBase = itemBaseCacheService.getOrSave(base);
    item.setBase(itemBase);

    return itemCacheService.getOrSave(item);
  }

}
