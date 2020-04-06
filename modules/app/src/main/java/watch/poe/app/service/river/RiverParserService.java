package watch.poe.app.service.river;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.PriceDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.RiverDto;
import watch.poe.app.dto.river.StashDto;
import watch.poe.app.dto.wrapper.ItemWrapper;
import watch.poe.app.dto.wrapper.RiverWrapper;
import watch.poe.app.dto.wrapper.StashWrapper;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.item.ItemDetailParserService;
import watch.poe.app.service.item.NoteParseService;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.code.ParseErrorCode;
import watch.poe.persistence.model.item.ItemDetail;
import watch.poe.persistence.model.leagueItem.LeagueItemEntry;
import watch.poe.persistence.utility.HashUtility;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.service.StatTimerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiverParserService {

  private final GsonService gsonService;
  private final StatTimerService statTimerService;
  private final NoteParseService noteParseService;
  private final ItemDetailParserService itemDetailParserService;

  @Value("${item.accept.missing.price}")
  private boolean acceptMissingPrice;

  @Async
  public Future<RiverWrapper> process(String job, StringBuilder stashStringBuilder) {
    statTimerService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
    RiverDto riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
    statTimerService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

    statTimerService.startTimer(StatType.TIME_REPLY_PARSE);
    RiverWrapper riverWrapper = processRiver(riverDto);
    statTimerService.clkTimer(StatType.TIME_REPLY_PARSE);

    riverWrapper.setJob(job);

    return CompletableFuture.completedFuture(riverWrapper);
  }

  private RiverWrapper processRiver(RiverDto riverDto) {
    var stashes = riverDto.getStashes().stream()
      .map(this::processStash)
      .collect(Collectors.toList());

    return RiverWrapper.builder()
      .stashes(stashes)
      .completionTime(LocalDateTime.now())
      .build();
  }

  private StashWrapper processStash(StashDto stashDto) {
    statTimerService.addValue(StatType.COUNT_TOTAL_ITEMS, stashDto.getItems().size());

    var stashWrapper = StashWrapper.builder()
      .id(stashDto.getId())
      .league(stashDto.getLeague())
      .account(stashDto.getAccountName())
      .character(stashDto.getLastCharacterName())
      .build();

    ArrayList<LeagueItemEntry> entries = new ArrayList<>();

    for (ItemDto itemDto : stashDto.getItems()) {
      PriceDto price = noteParseService.parsePrice(stashDto.getStashName(), itemDto.getNote());
      processItem(itemDto, price)
        .map(entries::add);
    }

    stashWrapper.setEntries(entries);
    return stashWrapper;
  }

  private Optional<LeagueItemEntry> processItem(ItemDto itemDto, PriceDto price) {
    if (price == null && !acceptMissingPrice) {
      return Optional.empty();
    }

    var itemWrapper = ItemWrapper.builder()
      .itemDto(itemDto)
      .discardReasons(new ArrayList<>())
      .itemDetail(ItemDetail.builder().build())
      .build();

    ItemDetail itemDetail, priceCurrencyItem;
    try {
      itemDetail = itemDetailParserService.parse(itemWrapper);
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

      return Optional.empty();
    } catch (GroupingException ex) {
      log.info("Grouping exception \"{}\" for {}", ex.getMessage(), itemWrapper);
      return Optional.empty();
    }

    if (itemWrapper.isDiscard()) {
      return Optional.empty();
    }

    var entry = LeagueItemEntry.builder()
      .id(HashUtility.hash(itemDto.getId()))
      .itemDetail(itemDetail)
      .price(price == null ? null : price.getPrice())
      .priceItem(priceCurrencyItem)
      .stackSize(itemDto.getStackSize())
      .stash(null)
      .found(LocalDateTime.now())
      .seen(LocalDateTime.now())
      .updates(0)
      .build();

    return Optional.of(entry);
  }

}
