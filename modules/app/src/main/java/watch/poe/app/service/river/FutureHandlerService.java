package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.dto.wrapper.RiverWrapper;
import watch.poe.app.dto.wrapper.StashWrapper;
import watch.poe.app.service.AccountService;
import watch.poe.app.service.CharacterService;
import watch.poe.app.service.LeagueItemEntryService;
import watch.poe.app.service.StashService;
import watch.poe.app.service.cache.*;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.Character;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.model.leagueItem.LeagueItemEntry;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.service.StatTimerService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutureHandlerService {

  private final StatTimerService statTimerService;
  private final StashService stashRepositoryService;
  private final AccountService accountService;
  private final LeagueItemEntryService leagueItemEntryService;

  private final CharacterService characterService;
  private final LeagueCacheService leagueCacheService;
  private final CategoryCacheService categoryCacheService;
  private final GroupCacheService groupCacheService;
  private final ItemBaseCacheService itemBaseCacheService;
  private final ItemDetailCacheService itemDetailCacheService;

  @Async
  public Future<String> process(List<RiverWrapper> wrappers) {
    statTimerService.startTimer(StatType.TIME_PROCESS_RIVER);

    var stashWrappers = wrappers.stream()
      .map(RiverWrapper::getStashes)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    statTimerService.startTimer(StatType.TIME_INDEX_ITEM);
    indexItems(stashWrappers);
    statTimerService.clkTimer(StatType.TIME_INDEX_ITEM, true);

    statTimerService.startTimer(StatType.TIME_PERSIST_ACCOUNT);
    var accounts = saveAccounts(stashWrappers);
    statTimerService.clkTimer(StatType.TIME_PERSIST_ACCOUNT, true);

    statTimerService.startTimer(StatType.TIME_PERSIST_CHARACTER);
    saveCharacters(stashWrappers, accounts);
    statTimerService.clkTimer(StatType.TIME_PERSIST_CHARACTER, true);

    statTimerService.startTimer(StatType.TIME_MARK_ITEMS_STALE);
    markItemsStale(stashWrappers);
    statTimerService.clkTimer(StatType.TIME_MARK_ITEMS_STALE, true);

    statTimerService.startTimer(StatType.TIME_PERSIST_STASHES);
    var stashes = saveStashes(stashWrappers, accounts);
    statTimerService.clkTimer(StatType.TIME_PERSIST_STASHES, true);

    statTimerService.startTimer(StatType.TIME_PERSIST_STASH_ENTRIES);
    saveEntries(stashWrappers, stashes);
    statTimerService.clkTimer(StatType.TIME_PERSIST_STASH_ENTRIES, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);

    statTimerService.clkTimer(StatType.TIME_PROCESS_RIVER);
    return CompletableFuture.completedFuture(newestJob);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected void indexItems(List<StashWrapper> stashWrappers) {
    var entries = stashWrappers.stream()
      .map(StashWrapper::getEntries)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    log.info("Starting index job with {} entries", entries.size());

    // todo: filter out duplicates
    for (LeagueItemEntry entry : entries) {
      var item = entry.getItemDetail();
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
      entry.setItemDetail(itemDetailCacheService.getOrSave(item));
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  protected List<Account> saveAccounts(List<StashWrapper> stashWrappers) {
    var validStashes = stashWrappers.stream()
      .filter(stash -> stash.getAccount() != null)
      .collect(Collectors.toList());

    var accountNames = validStashes.stream()
      .map(StashWrapper::getAccount)
      .distinct()
      .collect(Collectors.toList());

    return accountService.saveAll(accountNames);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  protected List<Character> saveCharacters(List<StashWrapper> stashWrappers, List<Account> accounts) {
    Set<String> distinctCharacterNames = new HashSet<>();
    var characters = stashWrappers.stream()
      .filter(stashWrapper -> stashWrapper.getAccount() != null)
      .filter(stashWrapper -> stashWrapper.getCharacter() != null)
      .filter(stashWrapper -> !distinctCharacterNames.contains(stashWrapper.getCharacter()))
      .peek(stashWrapper -> distinctCharacterNames.add(stashWrapper.getCharacter()))
      .map(stashWrapper -> {
        var account = accounts.stream()
          .filter(a -> a.getName().equals(stashWrapper.getAccount()))
          .findAny()
          .orElse(null);

        return Character.builder()
          .name(stashWrapper.getCharacter())
          .account(account)
          .found(LocalDateTime.now())
          .seen(LocalDateTime.now())
          .build();
      }).filter(character -> {
        if (character.getAccount() == null) {
          log.error("Expected to find a persisted account for character '{}' but didn't", character.getName());
          return false;
        }
        return true;
      }).collect(Collectors.toList());

    return characterService.saveAll(characters);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  protected void markItemsStale(List<StashWrapper> stashWrappers) {
//    var stashIds = stashWrappers.stream()
//      .collect(Collectors.toMap(StashWrapper::getId, stashWrapper -> {
//        return stashWrapper.getEntries().stream()
//          .mapToLong(LeagueItemEntry::getId)
//          .boxed()
//          .collect(Collectors.toList());
//      }));

    var stashIds = stashWrappers.stream()
      .map(StashWrapper::getId)
      .distinct()
      .collect(Collectors.toList());
    leagueItemEntryService.markStale(stashIds);
  }

  @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
  protected List<Stash> saveStashes(List<StashWrapper> stashWrappers, List<Account> accounts) {
    var validStashes = stashWrappers.stream()
      .filter(stash -> stash.getLeague() != null)
      .filter(stash -> stash.getAccount() != null)
      .filter(stash -> stash.getEntries() != null)
      .filter(stash -> !stash.getEntries().isEmpty())
      .map(stashWrapper -> {
        var league = leagueCacheService.get(stashWrapper.getLeague());
        var account = accounts.stream()
          .filter(a -> a.getName().equals(stashWrapper.getAccount()))
          .findFirst();
        return league.isEmpty() || account.isEmpty()
          ? null
          : Stash.builder()
          .poeId(stashWrapper.getId())
          .account(account.get())
          .league(league.get())
          .found(LocalDateTime.now())
          .seen(LocalDateTime.now())
          .updates(0)
          .stale(false)
          .build();
      }).filter(Objects::nonNull)
      .collect(Collectors.toList());
    return stashRepositoryService.saveAll(validStashes);
  }

  @Transactional(propagation = Propagation.REQUIRED, timeout = 120)
  protected void saveEntries(List<StashWrapper> stashWrappers, List<Stash> stashes) {
    // set stash for every entry
    stashWrappers.forEach(stashWrapper -> {
      var matchingDbStash = stashes.stream()
        .filter(stash -> stash.getId().equals(stashWrapper.getId()))
        .findAny();
      if (matchingDbStash.isEmpty()) {
        return;
      }
      var stash = matchingDbStash.get();
      stashWrapper.getEntries().forEach(entry -> entry.setStash(stash));
    });

    var entries = stashWrappers.stream()
      .filter(stashWrapper -> stashWrapper.getEntries() != null)
      .filter(stashWrapper -> !stashWrapper.getEntries().isEmpty())
      .map(StashWrapper::getEntries)
      .flatMap(Collection::stream)
      .filter(entry -> entry.getId() != null)
      .collect(Collectors.toList());

    leagueItemEntryService.saveAll(entries);
  }

}
