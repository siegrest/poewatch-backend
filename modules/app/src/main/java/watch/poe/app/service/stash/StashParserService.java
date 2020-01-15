package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.RiverDto;
import watch.poe.app.dto.river.StashDto;
import watch.poe.app.exception.ItemDiscardException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.LeagueService;
import watch.poe.app.service.NoteParseService;
import watch.poe.app.service.item.ItemParserService;
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.CharacterService;
import watch.poe.app.service.repository.StashRepositoryService;
import watch.poe.app.service.statistics.StatType;
import watch.poe.app.service.statistics.StatisticsService;
import watch.poe.persistence.model.Item;

@Slf4j
@Service
public class StashParserService {

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

  @Value("${item.accept.missing.price}")
  private boolean acceptMissingPrice;

  @Async
  public void process(StringBuilder stashStringBuilder) {
    statisticsService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
    var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
    statisticsService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

    log.info("got {} stashes", riverDto.getStashes().size());

    statisticsService.startTimer(StatType.TIME_REPLY_PARSE);
    processRiver(riverDto);
    statisticsService.clkTimer(StatType.TIME_REPLY_PARSE);
  }

  private void processRiver(RiverDto riverDto) {

    for (StashDto stashDto : riverDto.getStashes()) {
      statisticsService.addValue(StatType.COUNT_TOTAL_ITEMS, stashDto.getItems().size());

      var league = leagueService.getByName(stashDto.getLeague());
      if (league.isEmpty()) {
        log.info("invalid league {}", stashDto.getLeague());
        statisticsService.addValue(StatType.COUNT_ITEMS_DISCARDED_INVALID_LEAGUE, stashDto.getItems().size());
        continue;
      }

      var account = accountService.save(stashDto.getAccountName());
      var character = characterService.save(account, stashDto.getLastCharacterName());
      var stash = stashRepositoryService.save(league.get(), account, stashDto);

      if (character == null || account == null || stash == null) {
        log.info("skipping {} {} {}", character, account, stash);
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

        Item item;
        try {
          // todo: don't use exceptions to control the flow of the application
          item = itemParserService.parse(itemDto);
        } catch (ItemDiscardException ex) {
          log.info("Discard error {} for {}", ex.getMessage(), itemDto);
          continue;
        } catch (ItemParseException ex) {
          log.info("Parse error {} for {}", ex.getMessage(), itemDto);
          continue;
        }

        log.info("Accepted item {}", item);

//            if (item.isDiscard()) {
//                continue;
//            }
//
//            // Get item's ID (if missing, index it)
//            Integer id_d = ix.index(item, id_l);
//            if (id_d == null) continue;
//
//            // Calculate crc of item's ID
//            long itemCrc = Utility.calcCrc(apiItem.getId());
//
//            // Create DB entry object
//            DbItemEntry entry = new DbItemEntry(id_l, id_d, stash_crc, itemCrc, item.getStackSize(), price, user);
//
//            // If item should be recorded but should not have a price
//            if (item.isClearPrice() && cf.getBoolean("entry.removeEnchantedHelmetPrices")) {
//                entry.price = null;
//            }
//
//            // Set flag to indicate the stash contained at least 1 valid item
//            hasValidItems = true;
//            dbItems.add(entry);
      }
    }
  }

}
