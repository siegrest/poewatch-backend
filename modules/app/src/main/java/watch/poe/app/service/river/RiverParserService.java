package watch.poe.app.service.river;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.RiverDto;
import watch.poe.app.dto.river.StashDto;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.LeagueService;
import watch.poe.app.service.NoteParseService;
import watch.poe.app.service.item.ItemIndexerService;
import watch.poe.app.service.item.ItemParserService;
import watch.poe.app.service.item.ItemWrapper;
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.StashRepositoryService;
import watch.poe.app.service.statistics.StatType;
import watch.poe.app.service.statistics.StatisticsService;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.repository.LeagueItemEntryRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RiverParserService {

  @Autowired
  private GsonService gsonService;
  @Autowired
  private StatisticsService statisticsService;
  @Autowired
  private LeagueService leagueService;
  @Autowired
  private StashRepositoryService stashRepositoryService;
  @Autowired
  private AccountService accountService;
  @Autowired
  private CharacterService characterService;
  @Autowired
  private NoteParseService noteParseService;
  @Autowired
  private ItemParserService itemParserService;
  @Autowired
  private ItemIndexerService itemIndexerService;
  @Autowired
  private LeagueItemEntryRepository itemEntryRepository;

  @Value("${item.accept.missing.price}")
  private boolean acceptMissingPrice;

  @Async
  public void process(StringBuilder stashStringBuilder) {
    statisticsService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
    var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
    statisticsService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

    log.info("got {} stashes", riverDto.getStashes().size());

    statisticsService.startTimer(StatType.TIME_REPLY_PARSE);
    var riverWrappers = processRiver(riverDto);
    statisticsService.clkTimer(StatType.TIME_REPLY_PARSE);

    log.info("extracted {} valid items", riverWrappers.size());

    statisticsService.startTimer(StatType.TIME_REPLY_INDEX);
    processWrappers(riverWrappers);
    statisticsService.clkTimer(StatType.TIME_REPLY_INDEX);
  }

  private List<RiverWrapper> processRiver(RiverDto riverDto) {
    var riverWrappers = new ArrayList<RiverWrapper>();

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

        Item item;
        try {
          item = itemParserService.parse(wrapper);
        } catch (ItemParseException ex) {
          log.info("Parse exception \"{}\" for {}", ex.getMessage(), wrapper);
          continue;
        }

        if (wrapper.isDiscard()) {
          continue;
        }

        var riverWrapper = RiverWrapper.builder()
          .item(item)
          .league(league.get())
          .price(price)
          .stash(stash)
          .build();

        riverWrappers.add(riverWrapper);
      }
    }

    return riverWrappers;
  }

  private void processWrappers(List<RiverWrapper> riverWrappers) {

    for (var wrapper : riverWrappers) {
      var item = itemIndexerService.index(wrapper.getItem());

      var itemEntry = LeagueItemEntry.builder().build();

      // todo: persist itemEntry
    }

  }

}
