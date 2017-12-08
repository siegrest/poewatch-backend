package MainPack.MapperClasses;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    //  Name: Item
    //  Date created: 23.11.2017
    //  Last modified: 08.12.2017
    //  Description: Class used for deserializing a JSON string

    private int w;
    private int h;
    private int ilvl;
    private String icon;
    private String league;
    private String id;
    private String name;
    private String typeLine;
    private boolean identified = true;
    private boolean corrupted = false;
    private String note = "";
    private int frameType;
    private int x;
    private int y;
    private List<Properties> properties;
    private List<Socket> sockets;
    private List<String> explicitMods;

    private boolean discard = false;
    private double price;
    private String priceType;
    private String itemType;
    private String key = "";

    private static Map<String, String> currencyShorthandsMap = new TreeMap<>() {{
        put("exalt", "Exalted Orb");
        put("regret", "Orb of Regret");
        put("divine", "Divine Orb");
        put("chis", "Cartographer's Chisel");
        put("chao", "Chaos Orb");
        put("alchemy", "Orb of Alchemy");
        put("alts", "Orb of Alteration");
        put("fusing", "Orb of Fusing");
        put("fus", "Orb of Fusing");
        put("alteration", "Orb of Alteration");
        put("choas", "Chaos Orb");
        put("rega", "Regal Orb");
        put("gcp", "Gemcutter's Prism");
        put("regrets", "Orb of Regret");
        put("jeweller", "Jeweller's Orb");
        put("regal", "Regal Orb");
        put("chromatics", "Chromatic Orb");
        put("bles", "Blessed Orb");
        put("jewellers", "Jeweller's Orb");
        put("chance", "Orb of Chance");
        put("ex", "Exalted Orb");
        put("chromes", "Chromatic Orb");
        put("chanc", "Orb of Chance");
        put("chrom", "Chromatic Orb");
        put("exalted", "Exalted Orb");
        put("blessed", "Blessed Orb");
        put("c", "Chaos Orb");
        put("chaos", "Chaos Orb");
        put("chisel", "Cartographer's Chisel");
        put("alch", "Orb of Alchemy");
        put("exa", "Exalted Orb");
        put("vaal", "Vaal Orb");
        put("chrome", "Chromatic Orb");
        put("jew", "Jeweller's Orb");
        put("exalts", "Exalted Orb");
        put("scour", "Orb of Scouring");
        put("cart", "Cartographer's Chisel");
        put("alc", "Orb of Alchemy");
        put("fuse", "Orb of Fusing");
        put("exe", "Exalted Orb");
        put("jewel", "Jeweller's Orb");
        put("div", "Divine Orb");
        put("alt", "Orb of Alteration");
        put("fusings", "Orb of Fusing");
        put("chisels", "Cartographer's Chisel");
        put("chromatic", "Chromatic Orb");
        put("scouring", "Orb of Scouring");
        put("gemc", "Gemcutter's Prism");
        put("silver", "Silver Coin");
        put("aug", "Orb of Augmentation");
        put("mirror", "Mirror of Kalandra");
    }};
    private static Map<String, String> baseCurrencyIndexesMap = new TreeMap<>() {{
        put("Chaos Orb", "1");
        put("Exalted Orb", "2");
        put("Divine Orb", "3");
        put("Orb of Alchemy", "4");
        put("Orb of Fusing", "5");
        put("Orb of Alteration", "6");
        put("Regal Orb", "7");
        put("Vaal Orb", "8");
        put("Orb of Regret", "9");
        put("Cartographer's Chisel", "10");
        put("Jeweller's Orb", "11");
        put("Silver Coin", "12");
        put("Perandus Coin", "13");
        put("Orb of Scouring", "14");
        put("Gemcutter's Prism", "15");
        put("Orb of Chance", "16");
        put("Chromatic Orb", "17");
        put("Blessed Orb", "18");
        put("Glassblower's Bauble", "19");
        put("Orb of Augmentation", "20");
        put("Orb of Transmutation", "21");
        put("Mirror of Kalandra", "22");
        put("Scroll of Wisdom", "23");
        put("Portal Scroll", "24");
        put("Blacksmith's Whetstone", "25");
        put("Armourer's Scrap", "26");
        put("Apprentice Cartographer's Sextant", "27");
        put("Journeyman Cartographer's Sextant", "28");
        put("Master Cartographer's Sextant", "29");
    }};

    /////////////////////////////////////////////////////////
    // Methods used to convert/calculate/extract item data //
    /////////////////////////////////////////////////////////

    public void parseItem(){
        //  Name: parseItem()
        //  Date created: 08.12.2017
        //  Last modified: 08.12.2017
        //  Description: Calls other Item class related methods.

        // Do a few checks on the league, note and etc
        basicChecks();
        if (discard)
            return;

        // Get price as boolean and currency type as index
        parseNote();
        if (discard)
            return;

        // Make database key and find item type
        formatNameAndItemType();

        // Filter out white base types (but allow maps)
        if (frameType == 0 && !itemType.contains("maps"))
            return;

        // Check gem info or check links and variants
        if (frameType == 4) {
            checkGemInfo();
        } else {
            checkSixLink();
            checkSpecialItemVariant();
        }
    }

    private void basicChecks() {
        //  Name: basicChecks()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Method that does a few basic checks on items
        //  Parent methods:
        //      parseItem()

        if (note.equals("")) {
            // Filter out items without prices
            setDiscard();
        } else if (frameType == 1 || frameType == 2 || frameType == 7) {
            // Filter out unpriceable items
            setDiscard();
        } else if (!identified) {
            // Filter out unidentified items
            setDiscard();
        } else if (corrupted && frameType != 4) {
            // Filter out corrupted items besides gems
            setDiscard();
        } else if (league.contains("SSF")) {
            // Filter out SSF leagues as trading there is disabled
            setDiscard();
        }

        // TODO: add filter for enchanted items
    }

    private void parseNote() {
        //  Name: parseNote()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Checks and formats notes (user-inputted textfields that usually contain price data)
        //  Parent methods:
        //      parseItem()

        String[] noteList = note.split(" ");
        Double price;

        // Make sure note_list has 3 strings (eg ["~b/o", "5.3", "chaos"])
        if (noteList.length < 3) {
            setDiscard();
            return;
        } else if (!noteList[0].equalsIgnoreCase("~b/o") && !noteList[0].equalsIgnoreCase("~price")) {
            setDiscard();
            return;
        }

        // If the price has a ration then split it (eg ["5, 3"] with or ["24.3"] without a ration)
        String[] priceArray = noteList[1].split("/");

        // Try to figure out if price is numeric
        try {
            if (priceArray.length == 1)
                price = Double.parseDouble(priceArray[0]);
            else
                price = Double.parseDouble(priceArray[0]) / Double.parseDouble(priceArray[1]);
        } catch (Exception ex) {
            setDiscard();
            return;
        }

        // Assign price to item
        this.price = Math.round(price * 1000) / 1000.0;

        // See if the currency type listed is valid currency type
        if (!currencyShorthandsMap.containsKey(noteList[2])) {
            setDiscard();
            return;
        }

        // Add currency type to item
        this.priceType = baseCurrencyIndexesMap.get(currencyShorthandsMap.get(noteList[2]));
    }

    private void formatNameAndItemType() {
        //  Name: formatNameAndItemType()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Format the item's full name and finds the item type
        //  Parent methods:
        //      parseItem()

        // Start key with league
        addKey(league);

        // Get the item's type
        String[] splitItemType = icon.split("/");
        String itemType = splitItemType[splitItemType.length - 2];

        // Make sure even the weird items get a correct item type
        if (splitItemType[splitItemType.length - 1].equals("Item.png")) {
            itemType = "Flasks";
        } else if (frameType == 8) {
            // Prophecy items have the same icon category as currency
            itemType = "Prophecy";
        }

        // Set the value in the item object
        this.itemType = itemType;
        addKey("|" + itemType);

        // Format the name that will serve as the database key
        if (name.equals("")) {
            addKey("|" + typeLine);
        } else {
            addKey("|" + name);
            if (!typeLine.equals(""))
                addKey("|" + typeLine);
        }

        // Add frameType to key
        addKey("|" + frameType);
    }

    private void checkGemInfo() {
        //  Name: checkGemInfo()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Checks gem-specific information
        //  Parent methods:
        //      parseItem()

        int lvl = -1;
        int quality = 0;

        // Attempt to extract lvl and quality from item info
        for (Properties prop : properties) {
            if (prop.getName().equals("Level")) {
                lvl = Integer.parseInt(prop.getValues().get(0).get(0).split(" ")[0]);
            } else if (prop.getName().equals("Quality")) {
                quality = Integer.parseInt(prop.getValues().get(0).get(0).replace("+", "").replace("%", ""));
            }
        }

        // If quality or lvl was not found, return
        if (lvl == -1) {
            setDiscard();
            return;
        }

        // Begin the long block that filters out gems based on a number of properties
        if (name.equals("Empower Support") || name.equals("Enlighten Support") || name.equals("Enhance Support")) {
            if (corrupted) {
                if (lvl == 4 || lvl == 3)
                    quality = 0;
                else {
                    setDiscard();
                    return;
                }
            } else {
                if (quality < 10)
                    quality = 0;
                else if (quality > 17)
                    quality = 20;
                else {
                    setDiscard();
                    return;
                }
            }
        } else {
            if (corrupted) {
                if (itemType.equals("VaalGems")) {
                    if (lvl < 10 && quality == 20)
                        lvl = 0;
                    else if (lvl == 20 && quality == 20)
                        ; // TODO: improve this
                    else {
                        setDiscard();
                        return;
                    }
                } else {
                    if (lvl == 21 && quality == 20)
                        ;
                    else if (lvl == 20 && quality == 23)
                        ;
                    else if (lvl == 20 && quality == 20)
                        ;
                    else {
                        setDiscard();
                        return;
                    }
                }
            } else {
                if (lvl < 10 && quality == 20)
                    lvl = 0;
                else if (lvl == 20 && quality == 20)
                    ;
                else if (lvl == 20 && quality < 10)
                    quality = 0;
                else {
                    setDiscard();
                    return;
                }
            }
        }

        // Add the lvl and key to database key
        addKey("|" + lvl + "|" + quality);

        // Add corruption notifier
        if (corrupted)
            addKey("|1");
        else
            addKey("|0");


    }

    private void checkSixLink() {
        //  Name: checkSixLink()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Since 6-links are naturally more expensive, assign them a separate database key
        //  Parent methods:
        //      checkItem()

        // This is bad and I'm bad
        if(!itemType.equals("Staves") && !itemType.equals("BodyArmours") && !itemType.equals("TwoHandSwords"))
            if (!itemType.equals("TwoHandMaces") && !itemType.equals("TwoHandAxes") && !itemType.equals("Bows"))
                return;

        // Group links together
        Integer[] links = new Integer[]{0, 0, 0, 0, 0, 0};
        for (Socket socket : sockets) {
            links[socket.getGroup()]++;
        }

        // Find largest single link
        int maxLinks = 0;
        for (Integer link : links) {
            if (link > maxLinks)
                maxLinks = link;
        }

        // Update database key accordingly
        if (maxLinks == 6)
            addKey("|6L");
        else if (maxLinks == 5)
            addKey("|5L");
        else
            addKey("|0L");
    }

    private void checkSpecialItemVariant() {
        //  Name: checkSpecialItemVariant()
        //  Date created: 28.11.2017
        //  Last modified: 08.12.2017
        //  Description: Check if the item has a special variant, eg vessel of vinktar
        //  Parent methods:
        //      parseItem()

        String keySuffix = "";

        // Try to determine the type of Atziri's Splendour by looking at the item properties
        switch (name) {
            case "Atziri's Splendour":
                int armour = 0;
                int evasion = 0;
                int energy = 0;

                // Find each property's amount
                for (Properties prop : properties) {
                    switch (prop.getName()) {
                        case "Armour":
                            armour = Integer.parseInt(prop.getValues().get(0).get(0));
                            break;
                        case "Evasion Rating":
                            evasion = Integer.parseInt(prop.getValues().get(0).get(0));
                            break;
                        case "Energy Shield":
                            energy = Integer.parseInt(prop.getValues().get(0).get(0));
                            break;
                    }
                }

                // Run them through this massive IF block to determine the variant
                // Values taken from https://pathofexile.gamepedia.com/Atziri%27s_Splendour (at 29.11.2017)
                if (1052 <= armour && armour <= 1118) {
                    if (energy == 76) {
                        keySuffix = "|var(ar/ev/li)";
                    } else if (204 <= energy && energy <= 217) {
                        keySuffix = "|var(ar/es/li)";
                    } else if (428 <= energy && energy <= 489) {
                        keySuffix = "|var(ar/es)";
                    }
                } else if (armour > 1600) {
                    keySuffix = "|var(ar)";
                } else if (evasion > 1600) {
                    keySuffix = "|var(ev)";
                } else if (energy > 500) {
                    keySuffix = "|var(es)";
                } else if (1283 <= armour && armour <= 1513) {
                    keySuffix = "|var(ar/ev/es)";
                } else if (1052 <= evasion && evasion <= 1118) {
                    if (energy > 400) {
                        keySuffix = "|var(ev/es)";
                    } else {
                        keySuffix = "|var(ev/es/li)";
                    }
                }
                break;
            case "Vessel of Vinktar":
                // Attempt to match preset mod with item mod
                for (String itemMod : explicitMods) {
                    if(itemMod.contains("Lightning Damage to Spells")) {
                        keySuffix = "|var(spells)";
                        break;
                    } else if (itemMod.contains("Lightning Damage to Attacks")) {
                        keySuffix = "|var(attacks)";
                        break;
                    } else if (itemMod.contains("Converted to Lightning")) {
                        keySuffix = "|var(conversion)";
                        break;
                    } else if (itemMod.contains("Damage Penetrates")) {
                        keySuffix = "|var(penetration)";
                        break;
                    }
                }
                break;
            case "Doryani's Invitation":
                // Attempt to match preset mod with item mod
                for (String itemMod : explicitMods) {
                    if(itemMod.contains("increased Lightning Damage")) {
                        keySuffix = "|var(lightning)";
                        break;
                    } else if (itemMod.contains("increased Fire Damage")) {
                        keySuffix = "|var(fire)";
                        break;
                    } else if (itemMod.contains("increased Cold Damage")) {
                        keySuffix = "|var(cold)";
                        break;
                    } else if (itemMod.contains("increased Physical Damage")) {
                        keySuffix = "|var(physical)";
                        break;
                    }
                }
                break;
            case "Yriel's Fostering":
                // Attempt to match preset mod with item mod
                for (String itemMod : explicitMods) {
                    if(itemMod.contains("Chaos Damage to Attacks")) {
                        keySuffix = "|var(chaos)";
                        break;
                    } else if (itemMod.contains("Physical Damage to Attack")) {
                        keySuffix = "|var(physical)";
                        break;
                    } else if (itemMod.contains("increased Attack and Movement Speed")) {
                        keySuffix = "|var(speed)";
                        break;
                    }
                }
                break;
            case "Volkuur's Guidance":
                // Attempt to match preset mod with item mod
                for (String itemMod : explicitMods) {
                    if(itemMod.contains("Fire Damage to Spells")) {
                        keySuffix = "|var(fire)";
                        break;
                    } else if (itemMod.contains("Cold Damage to Spells")) {
                        keySuffix = "|var(cold)";
                        break;
                    } else if (itemMod.contains("Lightning Damage to Spells")) {
                        keySuffix = "|var(lightning)";
                        break;
                    }
                }
                break;
            default:
                return;
        }

        // Add new key suffix to existing key
        addKey(keySuffix);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Getters / Setters that do not have anything to do with the deserialization //
    ////////////////////////////////////////////////////////////////////////////////

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard() {
        this.discard = true;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getItemType() {
        return itemType;
    }

    public String getKey() {
        return key;
    }

    public void addKey(String partialKey) {
        this.key += partialKey;
    }

    ///////////////////////
    // Getters / Setters //
    ///////////////////////

    public String getId() {
        return id;
    }

    public int getH() {
        return h;
    }

    public int getIlvl() {
        return ilvl;
    }

    public int getW() {
        return w;
    }

    public int getFrameType() {
        return frameType;
    }

    public String getIcon() {
        return icon;
    }

    public String getLeague() {
        return league;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public String getTypeLine() {
        return typeLine;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public boolean isIdentified() {
        return identified;
    }

    public List<Properties> getProperties() {
        return properties;
    }

    public List<Socket> getSockets() {
        return sockets;
    }

    public List<String> getExplicitMods() {
        return explicitMods;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    public void setIlvl(int ilvl) {
        this.ilvl = ilvl;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public void setName(String name) {
        // This one is a bit longer but it's still a setter. Problem here is that some items come prefixed with
        // "<<set:MS>><<set:M>><<set:S>>" for whatever reason. This is frowned upon so it has to be removed.
        if (name.contains("<<set:MS>><<set:M>><<set:S>>"))
            this.name = name.replace("<<set:MS>><<set:M>><<set:S>>", "");
        else
            this.name = name;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTypeLine(String typeLine) {
        this.typeLine = typeLine;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setProperties(List<Properties> properties) {
        this.properties = properties;
    }

    public void setSockets(List<Socket> sockets) {
        this.sockets = sockets;
    }

    public void setExplicitMods(List<String> explicitMods) {
        this.explicitMods = explicitMods;
    }

}
