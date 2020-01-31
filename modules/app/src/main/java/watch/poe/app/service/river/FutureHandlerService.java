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
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.LeagueItemEntryService;
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
  private final LeagueItemEntryService itemEntryService;

  private final CategoryCacheService categoryCacheService;
  private final GroupCacheService groupCacheService;
  private final ItemBaseCacheService itemBaseCacheService;
  private final ItemCacheService itemCacheService;

  private final StashRepositoryService stashRepositoryService;
  private final AccountService accountService;
  private final CharacterService characterService;

  @Async
  @Transactional
  public Future<String> process(List<RiverWrapper> wrappers) {
    var stashes = wrappers.stream()
      .map(RiverWrapper::getStashes)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    var entries = stashes.stream()
      .map(Stash::getItems)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    statisticsService.startTimer(StatType.TIME_REPLY_INDEX);
    indexItems(entries);
    statisticsService.clkTimer(StatType.TIME_REPLY_INDEX, true);

    statisticsService.startTimer(StatType.TIME_REPLY_PERSIST);
    itemEntryService.saveAll(entries);
    statisticsService.clkTimer(StatType.TIME_REPLY_PERSIST, true);

    var newestJob = wrappers.stream()
      .map(RiverWrapper::getJob)
      .min(ChangeIdUtility::comparator)
      .orElse(null);


//    var account = accountService.save(stashDto.getAccountName());
//    var character = characterService.save(account, stashDto.getLastCharacterName());
//    var stash = stashRepositoryService.save(league.get(), account, stashDto);
//
//    if (character == null || account == null || stash == null) {
//      continue;
//    }

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

      itemCacheService.getOrSave(item);
    }
  }

  private void indexStashes(List<Stash> stashes) {
    var tmpAccounts = stashes.stream()
      .map(Stash::getAccount)
      .filter(account -> account.getName() != null)
      .collect(Collectors.toList());
    var tmpCharacters = tmpAccounts.stream()
      .map(Account::getCharacters)
      .flatMap(Collection::stream)
      .filter(character -> character.getName() != null)
      .collect(Collectors.toList());

    var accounts = tmpAccounts.stream()
      .peek(a -> a.setCharacters(null))
      .collect(Collectors.toList());
    var dbAccounts = accountService.saveAll(accounts);


//    var characters = accounts.stream()
//      .map(Account::getCharacters)
//      .flatMap(Collection::stream)
//      .filter(Objects::nonNull)
//      .collect(Collectors.toList());


    var characters = accounts.stream()
      .map(account -> account.getCharacters().stream()
        .peek(character -> dbAccounts.stream()
          .filter(a -> a.getName().equals(account.getName()))
          .findFirst()
          .ifPresent(character::setAccount))
        .collect(Collectors.toList()))
      .flatMap(Collection::stream)
      .filter(character -> character.getAccount() != null)
      .collect(Collectors.toList());

    // todo: broken code
    var dbCharacters = characterService.saveAll(characters);
    var stash = stashRepositoryService.save(league.get(), account, stashDto);

  }

}
