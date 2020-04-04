package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.RiverDto;
import watch.poe.app.dto.river.StashDto;
import watch.poe.app.dto.wrapper.ItemWrapper;
import watch.poe.app.dto.wrapper.RiverWrapper;
import watch.poe.app.dto.wrapper.StashWrapper;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.item.ItemParserService;
import watch.poe.app.service.item.NoteParseService;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.code.ParseErrorCode;
import watch.poe.persistence.model.item.Item;
import watch.poe.persistence.model.leagueItem.LeagueItemEntry;
import watch.poe.persistence.utility.HashUtility;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.service.StatTimerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverParserService {

  private final GsonService gsonService;
  private final StatTimerService statTimerService;
  private final NoteParseService noteParseService;
  private final ItemParserService itemParserService;

  @Value("${item.accept.missing.price}")
  private boolean acceptMissingPrice;

  @Async
//  @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
  public Future<RiverWrapper> process(String job, StringBuilder stashStringBuilder) {
    statTimerService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
    var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
    statTimerService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

    statTimerService.startTimer(StatType.TIME_REPLY_PARSE);
    var stashes = processRiver(riverDto);
    statTimerService.clkTimer(StatType.TIME_REPLY_PARSE);

    var wrapper = RiverWrapper.builder()
      .stashes(stashes)
      .job(job)
      .completionTime(LocalDateTime.now())
      .build();

    return CompletableFuture.completedFuture(wrapper);
  }

  private List<StashWrapper> processRiver(RiverDto riverDto) {
    var stashes = new ArrayList<StashWrapper>();

    for (StashDto stashDto : riverDto.getStashes()) {
      statTimerService.addValue(StatType.COUNT_TOTAL_ITEMS, stashDto.getItems().size());

      var stashWrapper = StashWrapper.builder()
        .id(stashDto.getId())
        .league(stashDto.getLeague())
        .account(stashDto.getAccountName())
        .character(stashDto.getLastCharacterName())
        .build();

      ArrayList<LeagueItemEntry> entries = new ArrayList<>();

      for (ItemDto itemDto : stashDto.getItems()) {
        var price = noteParseService.parsePrice(stashDto.getStashName(), itemDto.getNote());
        if (price == null && !acceptMissingPrice) {
          continue;
        }

        var itemWrapper = ItemWrapper.builder()
          .itemDto(itemDto)
          .discardReasons(new ArrayList<>())
          .item(Item.builder().build())
          .build();

        Item item, priceCurrencyItem;
        try {
          item = itemParserService.parse(itemWrapper);
          priceCurrencyItem = noteParseService.priceToItem(price);
        } catch (ItemParseException ex) {
          // todo: remove this
          if (ex.getParseErrorCode() != ParseErrorCode.MISSING_CURRENCY
            && ex.getParseErrorCode() != ParseErrorCode.PARSE_UNID_UNIQUE_ITEM
            && ex.getDiscardErrorCode() != DiscardErrorCode.PARSE_COMPLEX_MAGIC
            && ex.getDiscardErrorCode() != DiscardErrorCode.UNIQUE_ONLY
            && ex.getDiscardErrorCode() != DiscardErrorCode.PARSE_COMPLEX_RARE) {
            log.error("Parse exception for {}", itemWrapper, ex);
          }

          continue;
        } catch (GroupingException ex) {
          log.info("Grouping exception \"{}\" for {}", ex.getMessage(), itemWrapper);
          continue;
        }

        if (itemWrapper.isDiscard()) {
          continue;
        }

        var entry = LeagueItemEntry.builder()
          .id(HashUtility.hash(itemDto.getId()))
          .item(item)
          .price(price == null ? null : price.getPrice())
          .priceItem(priceCurrencyItem)
          .stackSize(itemDto.getStackSize())
          .stash(null)
          .found(LocalDateTime.now())
          .seen(LocalDateTime.now())
          .updates(0)
          .build();

        entries.add(entry);
      }

      stashWrapper.setEntries(entries);
      stashes.add(stashWrapper);
    }

    return stashes;
  }

}
