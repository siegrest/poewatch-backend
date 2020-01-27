package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.DiscardBasis;
import watch.poe.app.domain.ParseExceptionBasis;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.RiverDto;
import watch.poe.app.dto.river.StashDto;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.LeagueService;
import watch.poe.app.service.NoteParseService;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.item.ItemParserService;
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.StashRepositoryService;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.LeagueItemEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverParserService {

  private final GsonService gsonService;
  private final StatisticsService statisticsService;
  private final LeagueService leagueService;
  private final StashRepositoryService stashRepositoryService;
  private final AccountService accountService;
  private final CharacterService characterService;
  private final NoteParseService noteParseService;
  private final ItemParserService itemParserService;

  @Value("${item.accept.missing.price}")
  private boolean acceptMissingPrice;

  @Async
//  @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
  public Future<RiverWrapper> process(StringBuilder stashStringBuilder) {
    statisticsService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
    var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
    statisticsService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

    log.info("got {} stashes", riverDto.getStashes().size());

    statisticsService.startTimer(StatType.TIME_REPLY_PARSE);
    var entries = processRiver(riverDto);
    statisticsService.clkTimer(StatType.TIME_REPLY_PARSE);

    log.info("extracted {} valid items", entries.size());

    var wrapper = RiverWrapper.builder()
      .entries(entries)
      .completionTime(LocalDateTime.now())
      .build();

    return CompletableFuture.completedFuture(wrapper);
  }

  private Set<LeagueItemEntry> processRiver(RiverDto riverDto) {
    var entries = new HashSet<LeagueItemEntry>();

    for (StashDto stashDto : riverDto.getStashes()) {
      statisticsService.addValue(StatType.COUNT_TOTAL_ITEMS, stashDto.getItems().size());

      var league = leagueService.getByName(stashDto.getLeague());
      if (league.isEmpty()) {
        statisticsService.addValue(StatType.COUNT_ITEMS_DISCARDED_INVALID_LEAGUE, stashDto.getItems().size());
        continue;
      }

      var account = accountService.save(stashDto.getAccountName());
      var character = characterService.save(account, stashDto.getLastCharacterName());
      var stash = stashRepositoryService.save(league.get(), account, stashDto);

      if (character == null || account == null || stash == null) {
        continue;
      }

      for (ItemDto itemDto : stashDto.getItems()) {
        if (itemDto == null) {
          log.info("Null item");
          continue;
        }

        var price = noteParseService.parsePrice(stashDto.getStashName(), itemDto.getNote());
        if (price == null && !acceptMissingPrice) {
          continue;
        }

        var wrapper = ItemWrapper.builder()
          .itemDto(itemDto)
          .discardReasons(new ArrayList<>())
          .item(Item.builder().build())
          .build();

        Item item, priceCurrencyItem;
        try {
          item = itemParserService.parse(wrapper);
          priceCurrencyItem = noteParseService.priceToItem(price);
        } catch (ItemParseException ex) {
          // todo: remove this
          if (ex.getParseExceptionBasis() != ParseExceptionBasis.MISSING_CURRENCY
            && ex.getParseExceptionBasis() != ParseExceptionBasis.PARSE_UNID_UNIQUE_ITEM
            && ex.getDiscardBasis() != DiscardBasis.PARSE_COMPLEX_MAGIC
            && ex.getDiscardBasis() != DiscardBasis.UNIQUE_ONLY
            && ex.getDiscardBasis() != DiscardBasis.PARSE_COMPLEX_RARE) {
            log.info("Parse exception \"{}\" for {}", ex.getMessage(), wrapper);
          }

          continue;
        } catch (GroupingException ex) {
          log.info("Grouping exception \"{}\" for {}", ex.getMessage(), wrapper);
          continue;
        }

        if (wrapper.isDiscard()) {
          continue;
        }

        var entry = LeagueItemEntry.builder()
          .id(itemDto.getId())
          .item(item)
          .price(price == null ? null : price.getPrice())
          .priceItem(priceCurrencyItem)
          .stackSize(itemDto.getStackSize())
          .stash(stash)
          .build();

        entries.add(entry);
      }
    }

    return entries;
  }

//  private void processWrappers(List<LeagueItemEntry> entries) {
//    for (var entry : entries) {
//      var item = itemIndexerService.index(entry.getItem());
//      entry.setItem(item);
//      itemEntryService.save(entry);
//    }
//  }

}
