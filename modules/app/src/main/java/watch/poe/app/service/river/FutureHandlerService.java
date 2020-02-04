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
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.LeagueItemEntryService;
import watch.poe.app.service.repository.StashService;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.Character;
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
  private final StashService stashRepositoryService;
  private final AccountService accountService;
  private final LeagueItemEntryService leagueItemEntryService;

  private final CharacterService characterService;
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

    statisticsService.startTimer(StatType.TIME_INDEX_ITEM);
    indexItems(stashWrappers);
    statisticsService.clkTimer(StatType.TIME_INDEX_ITEM, true);

    statisticsService.startTimer(StatType.TIME_INDEX_ACCOUNT);
    var accounts = saveAccounts(stashWrappers);
    statisticsService.clkTimer(StatType.TIME_INDEX_ACCOUNT, true);

    statisticsService.startTimer(StatType.TIME_INDEX_CHARACTER);
    saveCharacters(stashWrappers, accounts);
    statisticsService.clkTimer(StatType.TIME_INDEX_CHARACTER, true);

    statisticsService.startTimer(StatType.TIME_INDEX_STASH);
    var stashes = saveStashes(stashWrappers, accounts);
    statisticsService.clkTimer(StatType.TIME_INDEX_STASH, true);

    statisticsService.startTimer(StatType.TIME_PERSIST_ENTRY);
    saveEntries(stashWrappers, stashes);
    statisticsService.clkTimer(StatType.TIME_PERSIST_ENTRY, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);

    statisticsService.clkTimer(StatType.TIME_PROCESS_RIVER);
    return CompletableFuture.completedFuture(newestJob);
  }

  private void indexItems(List<StashWrapper> stashWrappers) {
    var entries = stashWrappers.stream()
      .map(StashWrapper::getEntries)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

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

      item.setBase(itemBaseCacheService.getOrSave(base));
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

    return accountService.saveAll(accountNames);
  }

  private List<Character> saveCharacters(List<StashWrapper> stashWrappers, List<Account> accounts) {
    var validStashes = stashWrappers.stream()
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getCharacter() != null)
      .collect(Collectors.toList());

    var characters = validStashes.stream()
      .map(stashWrapper -> {
        var account = accounts.stream()
          .filter(a -> a.getName().equals(stashWrapper.getAccount()))
          .findAny()
          .orElse(null);

        return Character.builder()
          .name(stashWrapper.getCharacter())
          .account(account)
          .build();
      }).filter(character -> character.getAccount() != null)
      .collect(Collectors.toList());

    return characterService.saveAll(characters);
  }

  private List<Stash> saveStashes(List<StashWrapper> stashWrappers, List<Account> accounts) {
    var validStashes = stashWrappers.stream()
      .filter(stash -> stash.getLeague() != null)
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getEntries() != null)
      .filter(stash -> !stash.getEntries().isEmpty())
      .map(stashWrapper -> {
        var league = leagueCacheService.get(stashWrapper.getLeague())
          .orElse(null);
        var account = accounts.stream()
          .filter(a -> a.getName().equals(stashWrapper.getAccount()))
          .findFirst()
          .orElse(null);

        return Stash.builder()
          .id(stashWrapper.getId())
          .account(account)
          .league(league)
          .build();
      }).collect(Collectors.toList());

    var staleIds = stashWrappers.stream()
      .filter(stashWrapper -> validStashes.stream().noneMatch(s -> s.getId().equals(stashWrapper.getId())))
      .map(StashWrapper::getId)
      .collect(Collectors.toList());

    stashRepositoryService.markStale(staleIds);
    return stashRepositoryService.saveAll(validStashes);
  }

  private void saveEntries(List<StashWrapper> stashWrappers, List<Stash> stashes) {
    // set stash for every entry
    stashWrappers.forEach(stashWrapper -> {
      var matchingDbStash = stashes.stream().filter(stash -> stash.getId().equals(stashWrapper.getId()))
        .findAny();
      matchingDbStash.ifPresent(stash -> stashWrapper.getEntries().forEach(entry -> entry.setStash(stash)));
    });

    var entries = stashWrappers.stream()
      .map(StashWrapper::getEntries)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    var validEntries = entries.stream()
      .filter(e -> e.getStash() != null)
      .collect(Collectors.toList());
    var staleEntries = entries.stream()
      .filter(e -> !validEntries.contains(e))
      .map(LeagueItemEntry::getId)
      .collect(Collectors.toList());

    leagueItemEntryService.markStale(staleEntries);
    leagueItemEntryService.saveAll(entries);
  }

}
