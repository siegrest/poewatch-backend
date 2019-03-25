package poe.Item.Parser;

import com.typesafe.config.Config;
import poe.Db.Database;
import poe.Item.ApiDeserializers.ApiItem;
import poe.Item.ApiDeserializers.Reply;
import poe.Item.ApiDeserializers.Stash;
import poe.Item.PoeWatchItem;
import poe.Managers.LeagueManager;
import poe.Managers.RelationManager;
import poe.Managers.Stat.StatType;
import poe.Managers.StatisticsManager;
import poe.Worker.Entry.RawUsernameEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

public class ItemParser {
    private final Set<Long> dbStashes;
    private final LeagueManager leagueManager;
    private final RelationManager relationManager;
    private final StatisticsManager statisticsManager;
    private final Database database;
    private final Config config;
    private final CRC32 crc;

    public ItemParser(LeagueManager lm, RelationManager rm, Config cnf, StatisticsManager sm, Database db) {
        this.leagueManager = lm;
        this.relationManager = rm;
        this.config = cnf;
        this.statisticsManager = sm;
        this.database = db;

        dbStashes = new HashSet<>(100000);
        crc = new CRC32();
    }


    public Set<Long> getStashCrcSet() {
        return dbStashes;
    }

    private long calcCrc(String str) {
        if (str == null) {
            return 0;
        } else {
            crc.reset();
            crc.update(str.getBytes());
            return crc.getValue();
        }
    }


    /**
     * Parses the raw items found on the stash api
     */
    public void processApiReply(Reply reply) {
        // Set of account names and items extracted from the API call
        Set<Long> accounts = new HashSet<>();
        Set<Long> nullStashes = new HashSet<>();
        Set<DbItemEntry> items = new HashSet<>();
        // Separate set for collecting account and character names
        Set<RawUsernameEntry> usernames = new HashSet<>();
        int totalItemCount = 0;

        for (Stash stash : reply.stashes) {
            totalItemCount += stash.items.size();

            // Get league ID. If it's an unknown ID, skip this stash
            Integer id_l = leagueManager.getLeagueId(stash.league);
            if (id_l == null) {
                continue;
            }

            // Calculate CRCs
            long account_crc = calcCrc(stash.accountName);
            long stash_crc = calcCrc(stash.id);

            // If the stash is in use somewhere in the database
            synchronized (dbStashes) {
                if (dbStashes.contains(stash_crc)) {
                    nullStashes.add(stash_crc);
                }
            }

            if (stash.accountName == null || !stash.isPublic) {
                continue;
            }

            boolean hasValidItems = false;

            for (ApiItem apiItem : stash.items) {

                // Convert api items to poewatch items
                ArrayList<PoeWatchItem> poeWatchItems = convertApiItem(apiItem);
                if (poeWatchItems == null) continue;

                // Attempt to determine the price of the item
                Price price = new Price(apiItem.getNote(), stash.stashName);

                // If item didn't have a valid price
                if (!price.hasPrice() && !config.getBoolean("entry.acceptNullPrice")) {
                    continue;
                }

                // Parse branched items and create objects for db upload
                for (PoeWatchItem poeWatchItem : poeWatchItems) {
                    // Get item's ID (if missing, index it)
                    Integer id_d = relationManager.index(poeWatchItem, id_l);
                    if (id_d == null) continue;

                    DbItemEntry entry = new DbItemEntry(id_l, id_d, account_crc, stash_crc, calcCrc(apiItem.getId()),
                            poeWatchItem.getStackSize(), price);
                    items.add(entry);

                    // Set flag to indicate the stash contained at least 1 valid item
                    hasValidItems = true;
                }
            }

            // If stash contained at least 1 valid item, save the account
            if (hasValidItems) {
                dbStashes.add(stash_crc);
                accounts.add(account_crc);
            }

            // As this is a completely separate service, collect all character and account names separately
            if (stash.lastCharacterName != null) {
                usernames.add(new RawUsernameEntry(stash.accountName, stash.lastCharacterName, id_l));
            }
        }

        // Collect some statistics
        statisticsManager.addValue(StatType.COUNT_TOTAL_STASHES, reply.stashes.size());
        statisticsManager.addValue(StatType.COUNT_TOTAL_ITEMS, totalItemCount);
        statisticsManager.addValue(StatType.COUNT_ACCEPTED_ITEMS, items.size());

        // Shovel everything to db
        database.upload.uploadAccounts(accounts);
        database.flag.resetStashReferences(nullStashes);
        database.upload.uploadEntries(items);
        database.upload.uploadUsernames(usernames);
    }


    private ArrayList<PoeWatchItem> convertApiItem(ApiItem apiItem) {
        // Do a few checks on the league, note and etc
        if (checkIfDiscardApiItem(apiItem)) return null;

        // Branch item
        ArrayList<PoeWatchItem> branches = createBranches(apiItem);

        // Process the branches
        branches.forEach(PoeWatchItem::process);
        branches.removeIf(PoeWatchItem::isDiscard);

        return branches;
    }

    /**
     * Check if the item should be discarded immediately.
     */
    private boolean checkIfDiscardApiItem(ApiItem apiItem) {
        // Filter out items posted on the SSF leagues
        if (apiItem.getLeague().contains("SSF")) {
            return true;
        }

        // Filter out a specific bug in the API
        if (apiItem.getLeague().equals("false")) {
            return true;
        }

        // Race rewards usually cost tens of times more than the average for their sweet, succulent altArt
        return apiItem.isRaceReward() != null && apiItem.isRaceReward();

    }

    /**
     * Check if item should be branched (i.e there could be more than one database entry from that item)
     */
    private ArrayList<PoeWatchItem> createBranches(ApiItem apiItem) {
        ArrayList<PoeWatchItem> branches = new ArrayList<>();

        // Default item
        PoeWatchItem item = new PoeWatchItem(BranchType.none, apiItem);
        branches.add(item);

        // If item is enchanted
        if (apiItem.getEnchantMods() != null) {
            item = new PoeWatchItem(BranchType.enchantment, apiItem);
            branches.add(item);
        }

        // If item is a crafting base
        if (apiItem.getFrameType() < 3 && apiItem.getIlvl() >= 68) {
            item = new PoeWatchItem(BranchType.base, apiItem);
            branches.add(item);
        }

        return branches;
    }
}