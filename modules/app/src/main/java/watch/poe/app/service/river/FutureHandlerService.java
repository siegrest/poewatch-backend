package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.domain.wrapper.StashWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.cache.*;
import watch.poe.app.service.repository.AccountService;
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
  private final LeagueCacheService leagueCacheService;

  private final CategoryCacheService categoryCacheService;
  private final GroupCacheService groupCacheService;
  private final ItemBaseCacheService itemBaseCacheService;
  private final ItemCacheService itemCacheService;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Future<String> process(List<RiverWrapper> wrappers) {
    statisticsService.startTimer(StatType.TIME_PROCESS_RIVER);

    var stashWrappers = wrappers.stream()
      .map(RiverWrapper::getStashes)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    var entries = stashWrappers.stream()
      .map(StashWrapper::getEntries)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    statisticsService.startTimer(StatType.TIME_INDEX_ITEM);
    indexItems(entries);
    statisticsService.clkTimer(StatType.TIME_INDEX_ITEM, true);

    statisticsService.startTimer(StatType.TIME_INDEX_ACCOUNT);
    var accounts = saveAccounts(stashWrappers);
    statisticsService.clkTimer(StatType.TIME_INDEX_ACCOUNT, true);

    statisticsService.startTimer(StatType.TIME_INDEX_STASH);
    saveStashes(stashWrappers, accounts);
    statisticsService.clkTimer(StatType.TIME_INDEX_STASH, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);

    statisticsService.clkTimer(StatType.TIME_PROCESS_RIVER);
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

  private List<Account> saveAccounts(List<StashWrapper> stashWrappers) {
    var validStashes = stashWrappers.stream()
      .filter(stash -> stash.getAccount() != null)
      .collect(Collectors.toList());

    var accountNames = validStashes.stream()
      .map(StashWrapper::getAccount)
      .distinct()
      .collect(Collectors.toList());

    // todo: save characters

    return accountService.saveAll(accountNames);
  }

  private void saveStashes(List<StashWrapper> stashes, List<Account> accounts) {
    var validStashes = stashes.stream()
      .filter(stash -> stash.getLeague() != null)
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getEntries() != null)
      .filter(stash -> !stash.getEntries().isEmpty())
      .map(stashWrapper -> {
        var league = leagueCacheService.get(stashWrapper.getLeague());
        var account = accounts.stream()
          .filter(a -> a.getName().equals(stashWrapper.getAccount()))
          .findFirst();
        return Stash.builder()
          .id(stashWrapper.getId())
          .account(account.orElse(null))
          .items(stashWrapper.getEntries())
          .league(league.orElse(null))
          .build();
      })
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getLeague() != null)
      .collect(Collectors.toList());

    var invalidStashes = stashes.stream()
      .map(StashWrapper::getId)
      .filter(id -> validStashes.stream().noneMatch(s -> s.getId().equals(id)))
      .collect(Collectors.toList());

    stashRepositoryService.deleteAll(invalidStashes);
    stashRepositoryService.saveAll(validStashes);
  }

}
