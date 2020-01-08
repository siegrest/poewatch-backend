package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.RiverDto;
import watch.poe.app.dto.RiverItemDto;
import watch.poe.app.dto.RiverStashDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.ItemParseService;
import watch.poe.app.service.NoteParseService;
import watch.poe.app.service.repository.AccountService;
import watch.poe.app.service.repository.LeagueRepositoryService;
import watch.poe.app.service.repository.StashRepositoryService;
import watch.poe.app.service.statistics.StatType;
import watch.poe.app.service.statistics.StatisticsService;

@Slf4j
@Service
public class StashParserService {

    @Autowired
    private GsonService gsonService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private LeagueRepositoryService leagueRepositoryService;
    @Autowired
    private StashRepositoryService stashRepositoryService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private NoteParseService noteParseService;
    @Autowired
    private ItemParseService itemParseService;

    @Value("${item.accept.missing.price}")
    private boolean acceptMissingPrice;

    @Async
    public void process(StringBuilder stashStringBuilder) {
        statisticsService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
        var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
        statisticsService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

        statisticsService.startTimer(StatType.TIME_REPLY_PARSE);
        log.info("got {} stashes", riverDto.getStashes().size());
        processRiver(riverDto);
        statisticsService.clkTimer(StatType.TIME_REPLY_PARSE);
    }

    private void processRiver(RiverDto riverDto) {

        for (RiverStashDto riverStashDto : riverDto.getStashes()) {
            statisticsService.addValue(StatType.COUNT_TOTAL_ITEMS, riverStashDto.getItems().size());

            if (!leagueRepositoryService.isValidLeague(riverStashDto.getLeague())) {
                continue;
            }

            // todo: this comes later
//            var stash = stashRepositoryService.findByStashId(riverStashDto.getId());
//            if (stash.isEmpty()) {
//                stashRepositoryService.create(riverStashDto);
//            }

//            // Skip if missing data
//            if (stash.accountName == null || !stash.isPublic) {
//                continue;
//            }

            accountService.save(riverStashDto);
            var hasValidItems = processStashes(riverStashDto);

            // If stash contained at least 1 valid item, save the stash id
//            if (hasValidItems) {
//                activeStashIds.add(stash_crc);
//            }
        }
    }

    private boolean processStashes(RiverStashDto riverStashDto) {
        var hasValidItems = false;

        for (RiverItemDto itemDto : riverStashDto.getItems()) {
            var price = noteParseService.parsePrice(riverStashDto.getStashName(), itemDto.getNote());
            if (!acceptMissingPrice && price == null) {
                continue;
            }

            hasValidItems = itemParseService.parse(itemDto);

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

        return hasValidItems;
    }

}
