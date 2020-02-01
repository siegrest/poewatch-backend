package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.cache.CategoryCacheService;
import watch.poe.app.service.cache.GroupCacheService;
import watch.poe.app.service.cache.ItemBaseCacheService;
import watch.poe.app.service.cache.ItemCacheService;
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.StashRepositoryService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.model.Stash;

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
  private final StashRepositoryService stashRepositoryService;
  private final AccountService accountService;
  private final CharacterService characterService;

  private final CategoryCacheService categoryCacheService;
  private final GroupCacheService groupCacheService;
  private final ItemBaseCacheService itemBaseCacheService;
  private final ItemCacheService itemCacheService;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public Future<String> process(List<RiverWrapper> wrappers) {
    statisticsService.startTimer(StatType.TIME_PROCESS_RIVER);

    var stashes = wrappers.stream()
      .map(RiverWrapper::getStashes)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    var entries = stashes.stream()
      .map(Stash::getItems)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    statisticsService.startTimer(StatType.TIME_INDEX_ITEM);
    indexItems(entries);
    statisticsService.clkTimer(StatType.TIME_INDEX_ITEM, true);

    statisticsService.startTimer(StatType.TIME_INDEX_ACCOUNT);
    saveAccounts(stashes);
    statisticsService.clkTimer(StatType.TIME_INDEX_ACCOUNT, true);

    statisticsService.startTimer(StatType.TIME_INDEX_STASH);
    saveStashes(stashes);
    statisticsService.clkTimer(StatType.TIME_INDEX_STASH, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);

    statisticsService.clkTimer(StatType.TIME_PROCESS_RIVER, true);
    return CompletableFuture.completedFuture(newestJob);
  }

  private void indexItems(List<LeagueItemEntry> entries) {
    log.info("Starting index job with {} entries", entries.size());

    // todo: filter out duplicates
    for (LeagueItemEntry entry : entries) {
      var item = entry.getItem();
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

      // not necessary to set. saving updates object as well
      entry.setItem(itemCacheService.getOrSave(item));
    }
  }

  private void saveAccounts(List<Stash> stashes) {
    var validStashes = stashes.stream()
      .filter(stash -> stash.getAccount() != null)
      .collect(Collectors.toList());

    var accountNames = validStashes.stream()
      .map(Stash::getAccount)
      .map(Account::getName)
      .distinct()
      .collect(Collectors.toList());

    var dbAccounts = accountNames.stream()
      .map(accountService::save)
      .collect(Collectors.toList());

    validStashes.forEach(stash -> {
      dbAccounts.stream()
        .filter(dbAccount -> dbAccount.getName().equals(stash.getAccount().getName()))
        .findFirst()
        .ifPresentOrElse(stash::setAccount, () -> stash.setAccount(null));
    });
  }

  private void saveStashes(List<Stash> stashes) {
    var validStashes = stashes.stream()
      .filter(stash -> stash.getLeague() != null)
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getItems() != null)
      .filter(stash -> !stash.getItems().isEmpty())
      .collect(Collectors.toList());

    var invalidStashes = stashes.stream()
      .filter(stash -> !validStashes.contains(stash))
      .collect(Collectors.toList());

    stashRepositoryService.deleteAll(invalidStashes);
    stashRepositoryService.saveAll(validStashes);
  }

}
