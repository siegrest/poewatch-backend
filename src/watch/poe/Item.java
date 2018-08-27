package watch.poe;

import watch.poe.pricer.RawEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extends the JSON mapper Item, adding methods that parse, match and calculate Item-related data
 */
public class Item {
    //------------------------------------------------------------------------------------------------------------
    // Base item variables
    //------------------------------------------------------------------------------------------------------------

    private boolean identified;
    private int w, h, x, y, ilvl, frameType;
    private Boolean corrupted, shaper, elder;
    private String icon, league, id, name, typeLine, note;
    private Map<String, List<String>> category; // TODO: create an object for this monstrosity
    private List<Mappers.Property> properties;
    private List<Mappers.Socket> sockets;
    private List<String> explicitMods;
    private List<String> enchantMods;

    //------------------------------------------------------------------------------------------------------------
    // User-defined variables
    //------------------------------------------------------------------------------------------------------------

    private volatile boolean discard = false;
    private boolean doNotIndex, enchanted;
    private String priceType, parentCategory, childCategory, variation, key;
    private String links, level, quality, tier;
    private double price;

    //------------------------------------------------------------------------------------------------------------
    // Main methods
    //------------------------------------------------------------------------------------------------------------

    /**
     * Controller method, checks whether to keep item. If yes, preps item for db insertion
     */
    public void parseItem() {
        // Do a few checks on the league, note and etc
        basicChecks();
        if (discard) return;

        // Fix problematic data from the API
        fixData();

        // Extract price and currency type from item if present
        parseNote();
        if (discard) return;

        // Find out the item category (eg armour/belt/weapon etc)
        parseCategory();

        // Manually categorize some item types as the solution offered by GGG is not that great
        formatNameAndItemType();
        if (discard) return;

        // Call methods based on item's frametype
        switch (frameType) {
            case 0:
            case 1:
            case 2:
                if (enchanted) {
                    checkEnchant();
                    break;
                }

                // If it's not a map, discard it
                if (!parentCategory.equals("maps")) {
                    discard = true;
                    return;
                }

                // "Superior Ashen Wood" = "Ashen Wood"
                if (name.contains("Superior ")) name = name.replace("Superior ", "");

                // Include maps under same frame type
                frameType = 0;
                break;

            case 4: // Gem
                checkGemInfo();
                break;

            case 5: // Filter out chaos orbs
                // Discard specific currency items
                checkCurrency();
                break;

            default: // Everything else will pass through here
                checkSixLink();
                checkSpecialItemVariant();
                break;
        }

        // Attempt to find map tier from properties
        if (parentCategory.equals("maps") && properties != null) {
            for (Mappers.Property prop : properties) {
                if (prop.name.equals("Map Tier")) {
                    try {
                        tier = prop.values.get(0).get(0);
                    } catch (Exception ex) {
                        Main.ADMIN.log_("Couldn't parse tier:", 2);
                        Main.ADMIN._log(ex, 2);
                    }
                    break;
                }
            }
        }

        // Form the database key
        buildKey();
    }

    /**
     * Form the unique database key
     */
    private void buildKey() {
        StringBuilder key = new StringBuilder();

        // Add item's id
        key.append(name);

        // If present, add typeline to database key
        if (typeLine != null) {
            key.append(':');
            key.append(typeLine);
        }

        // Add item's frametype to database key
        key.append('|');
        key.append(frameType);

        // If the item has a 5- or 6-link
        if (links != null) {
            key.append("|links:");
            key.append(links);
        }

        // If the item has a variation
        if (variation != null) {
            key.append("|var:");
            key.append(variation);
        }

        // If the item was a gem, add gem info
        if (parentCategory.equals("gems")) {
            key.append("|l:");
            key.append(level);
            key.append("|q:");
            key.append(quality);
            key.append("|c:");
            key.append(corrupted ? 1 : 0);
        }

        this.key = key.toString();
    }

    /**
     * Uses provided currencyMap to covert Item's price to chaos
     *
     * @param currencyMap Map of currency name - chaos value relations
     */
    public void convertPrice(Map<String, Double> currencyMap) {
        // If the Item's price is not in chaos, it needs to be converted to chaos using the currency map
        if (!priceType.equals("Chaos Orb")) {
            // Precaution
            if (currencyMap == null) {
                discard = true;
                return;
            }

            Double chaosValue = currencyMap.get(priceType);

            if (chaosValue == null) {
                discard = true;
                return;
            }

            price = Math.round(price * chaosValue * Config.item_pricePrecision) / Config.item_pricePrecision;
            priceType = "Chaos Orb";
        }

        // User has specified a retarded price
        if (price < 0.0001 || price > 120000) {
            discard = true;
            return;
        }
    }

    //------------------------------------------------------------------------------------------------------------
    // Child methods
    //------------------------------------------------------------------------------------------------------------

    /**
     * Does a few basic checks on items
     */
    private void basicChecks() {
        // No price set on item
        if (note == null || note.equals("")) {
            discard = true;
            return;
        }

        // If item is enchanted, set its frame to 0 as we only care about the enchantment
        if (enchantMods != null) {
            enchanted = true;
            frameType = 0;
        }

        // Filter out magic/rare/quest items
        if (frameType == 1 || frameType == 2 || frameType == 7) {
            discard = true;
            return;
        }

        // Filter out unidentified items
        if (!identified) {
            discard = true;
            return;
        }

        // Filter out items posted on the SSF leagues
        if (league.contains("SSF")) {
            discard = true;
            return;
        }

        // Filter out a specific bug in the API
        if (league.equals("false")) {
            discard = true;
            return;
        }
    }

    /**
     * Fix problematic data from the API
     */
    private void fixData() {
        // Don't need the whole 64bit ID, half will do
        id = id.substring(0, 32);

        // Most items come with a "<<set:MS>><<set:M>><<set:S>>" or similar prefix
        name = name.substring(name.lastIndexOf(">") + 1);
    }

    /**
     *  Extract price and currency type from item if present
     */
    private void parseNote() {
        String[] noteList = note.split(" ");

        // Make sure note_list has 3 strings (eg ["~b/o", "5.3", "chaos"])
        if (noteList.length < 3 || !noteList[0].equals("~b/o") && !noteList[0].equals("~price")) {
            discard = true;
            return;
        }

        // If the price has a ration then split it (eg ["5, 3"] with or ["24.3"] without a ration)
        String[] priceArray = noteList[1].split("/");

        // Try to figure out if price is numeric
        Double price;
        try {
            if (priceArray.length == 1)
                price = Double.parseDouble(priceArray[0]);
            else
                price = Double.parseDouble(priceArray[0]) / Double.parseDouble(priceArray[1]);
        } catch (Exception ex) {
            discard = true;
            return;
        }

        // See if the currency type listed is valid currency type
        if (!Main.RELATIONS.getCurrencyAliasToName().containsKey(noteList[2])) {
            discard = true;
            return;
        }

        // Add currency type to item
        // If the seller is selling Chaos Orbs (the default currency), swap the places of the names
        // Ie [1 Chaos Orb]+"~b/o 6 fus" ---> [6 Orb of Fusing]+"~b/o 1 chaos"
        if (typeLine.equals("Chaos Orb")) {
            typeLine = Main.RELATIONS.getCurrencyAliasToName().get(noteList[2]);
            priceType = "Chaos Orb";
            this.price = (Math.round((1 / price) * Config.item_pricePrecision) / Config.item_pricePrecision);
            // Prevents other currency items getting Chaos Orb's icon
            doNotIndex = true;
        } else {
            this.price = Math.round(price * Config.item_pricePrecision) / Config.item_pricePrecision;
            priceType = Main.RELATIONS.getCurrencyAliasToName().get(noteList[2]);
        }
    }

    /**
     * Gets text-value(s) from category object
     */
    private void parseCategory() {
        parentCategory = category.keySet().toArray()[0].toString();

        if (category.get(parentCategory).size() > 0) {
            childCategory = category.get(parentCategory).get(0).toLowerCase();
        }
    }

    /**
     * Format the item's full id and finds the item type
     */
    private void formatNameAndItemType() {
        // Format item id and/or typeline
        if (name.equals("")) {
            name = typeLine;
            typeLine = null;
        }

        // Get item's icon entries-category
        String iconCategory;
        if (icon == null) {
            // Misc currency
            iconCategory = "currency";
        } else {
            String[] splitItemType = icon.split("/");
            iconCategory = splitItemType[splitItemType.length - 2].toLowerCase();
        }

        // Divide certain items to different entries-categories
        switch (parentCategory) {
            case "currency":
                // Prophecy items have the same icon category as currency
                if (frameType == 8) parentCategory = "prophecy";
                else if (iconCategory.equals("essence")) parentCategory = "essence";
                else if (iconCategory.equals("piece")) parentCategory = "piece";
                break;
            case "gems":
                // Put vaal gems into separate entries-category
                if (childCategory.equals("activegem") && iconCategory.equals("vaalgems"))
                    childCategory = "vaalgem";
                break;
            case "monsters":
                // Completely ignore monsters
                discard = true;
                break;
            case "maps":
                // Filter all unique maps under "unique" subcategory
                if (frameType == 3) childCategory = "unique";
                else if (iconCategory.equals("breach")) childCategory = "fragment";
                else if (properties == null) childCategory = "fragment";
                else childCategory = "map";
                break;
        }
    }

    /**
     * Checks gem-specific information
     */
    private void checkGemInfo() {
        int lvl = -1;
        int qual = 0;
        boolean corrupted = false;

        // Attempt to extract lvl and quality from item info
        for (Mappers.Property prop : properties) {
            if (prop.name.equals("Level")) {
                lvl = Integer.parseInt(prop.values.get(0).get(0).split(" ")[0]);
            } else if (prop.name.equals("Quality")) {
                qual = Integer.parseInt(prop.values.get(0).get(0).replace("+", "").replace("%", ""));
            }
        }

        // If quality or lvl was not found, return
        if (lvl == -1) {
            discard = true;
            return;
        }

        // Begin the long block that filters out gems based on a number of properties
        if (name.equals("Empower Support") || name.equals("Enlighten Support") || name.equals("Enhance Support")) {
            if (qual < 10) qual = 0;
            else qual = 20;

            // Quality doesn't matter for lvl 3 and 4
            if (lvl > 2) {
                qual = 0;

                if (this.corrupted != null) corrupted = this.corrupted;
            }
        } else {
            if (lvl < 19) lvl = 1;          // lvl       1 = 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18
            else if (lvl < 21) lvl = 20;    // lvl      20 = 19,20
                                            // lvl      21 = 21

            if (qual < 17) qual = 0;        // quality   0 = 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
            else if (qual < 22) qual = 20;  // quality  20 = 17,18,19,20,21
            else qual = 23;                 // quality  23 = 22,23

            // Gets rid of specific gems
            if (lvl < 20 && qual > 20) qual = 20;  // lvl:1 quality:23-> lvl:1 quality:20

            if (lvl > 20 || qual > 20) corrupted = true;
            else if (name.contains("Vaal")) corrupted = true;
        }

        this.level = Integer.toString(lvl);
        this.quality = Integer.toString(qual);
        this.corrupted = corrupted;
    }

    /**
     * Check if item can have a 6-link, assign them a separate key
     */
    private void checkSixLink() {
        if (childCategory == null) return;

        // Filter out items that can have 6 links
        switch (childCategory) {
            case "chest":
            case "staff":
            case "twosword":
            case "twomace":
            case "twoaxe":
            case "bow":
                break;
            default:
                return;
        }

        // This was an error somehow, somewhere
        if (sockets == null) return;

        // Group links together
        Integer[] linkArray = new Integer[]{0, 0, 0, 0, 0, 0};
        for (Mappers.Socket socket : sockets) {
            linkArray[socket.group]++;
        }

        // Find largest single link
        int largestLink = 0;
        for (Integer link : linkArray) {
            if (link > largestLink) {
                largestLink = link;
            }
        }

        if (largestLink > 4) links = Integer.toString(largestLink);
    }

    /**
     * Check if item has a variants (e.g. Vessel of Vinktar)
     */
    private void checkSpecialItemVariant() {
        switch (name) {
            // Try to determine the type of Atziri's Splendour by looking at the item explicit mods
            case "Atziri's Splendour":
                switch (String.join("#", explicitMods.get(0).split("\\d+"))) {
                    case "#% increased Armour, Evasion and Energy Shield":
                        variation = "ar/ev/es";
                        break;

                    case "#% increased Armour and Energy Shield":
                        if (explicitMods.get(1).contains("Life"))
                            variation = "ar/es/li";
                        else
                            variation = "ar/es";
                        break;

                    case "#% increased Evasion and Energy Shield":
                        if (explicitMods.get(1).contains("Life"))
                            variation = "ev/es/li";
                        else
                            variation = "ev/es";
                        break;

                    case "#% increased Armour and Evasion":
                        variation = "ar/ev";
                        break;

                    case "#% increased Armour":
                        variation = "ar";
                        break;

                    case "#% increased Evasion Rating":
                        variation = "ev";
                        break;

                    case "+# to maximum Energy Shield":
                        variation = "es";
                        break;
                }
                break;

            case "Vessel of Vinktar":
                // Attempt to match preset mod with item mod
                for (String explicitMod : explicitMods) {
                    if (explicitMod.contains("Lightning Damage to Spells")) {
                        variation = "spells";
                        break;
                    } else if (explicitMod.contains("Lightning Damage to Attacks")) {
                        variation = "attacks";
                        break;
                    } else if (explicitMod.contains("Converted to Lightning")) {
                        variation = "conversion";
                        break;
                    } else if (explicitMod.contains("Damage Penetrates")) {
                        variation = "penetration";
                        break;
                    }
                }
                break;

            case "Doryani's Invitation":
                // Attempt to match preset mod with item mod
                for (String explicitMod : explicitMods) {
                    if (explicitMod.contains("increased Lightning Damage")) {
                        variation = "lightning";
                        break;
                    } else if (explicitMod.contains("increased Fire Damage")) {
                        variation = "fire";
                        break;
                    } else if (explicitMod.contains("increased Cold Damage")) {
                        variation = "cold";
                        break;
                    } else if (explicitMod.contains("increased Global Physical Damage")) {
                        variation = "physical";
                        break;
                    }
                }
                break;

            case "Yriel's Fostering":
                // Attempt to match preset mod with item mod
                for (String explicitMod : explicitMods) {
                    if (explicitMod.contains("Bestial Snake")) {
                        variation = "snake";
                        break;
                    } else if (explicitMod.contains("Bestial Ursa")) {
                        variation = "ursa";
                        break;
                    } else if (explicitMod.contains("Bestial Rhoa")) {
                        variation = "rhoa";
                        break;
                    }
                }
                break;

            case "Volkuur's Guidance":
                // Attempt to match preset mod with item mod
                for (String explicitMod : explicitMods) {
                    if (explicitMod.contains("Fire Damage to Spells")) {
                        variation = "fire";
                        break;
                    } else if (explicitMod.contains("Cold Damage to Spells")) {
                        variation = "cold";
                        break;
                    } else if (explicitMod.contains("Lightning Damage to Spells")) {
                        variation = "lightning";
                        break;
                    }
                }
                break;

            case "Impresence":
                // Attempt to match preset mod with item mod
                for (String explicitMod : explicitMods) {
                    if (explicitMod.contains("Lightning Damage")) {
                        variation = "lightning";
                        break;
                    } else if (explicitMod.contains("Fire Damage")) {
                        variation = "fire";
                        break;
                    } else if (explicitMod.contains("Cold Damage")) {
                        variation = "cold";
                        break;
                    } else if (explicitMod.contains("Physical Damage")) {
                        variation = "physical";
                        break;
                    } else if (explicitMod.contains("Chaos Damage")) {
                        variation = "chaos";
                        break;
                    }
                }
                break;

            case "Lightpoacher":
            case "Shroud of the Lightless":
            case "Bubonic Trail":
            case "Tombfist":
                if (explicitMods.get(0).equals("Has 1 Abyssal Socket"))
                    variation = "1 socket";
                else if (explicitMods.get(0).equals("Has 2 Abyssal Sockets"))
                    variation = "2 sockets";
                break;

            case "The Beachhead":
                // Attempt to find map tier
                for (Mappers.Property property : properties) {
                    if (property.name.equals("Map Tier")) {
                        if (!property.values.isEmpty()) {
                            if (!property.values.get(0).isEmpty()) {
                                variation = property.values.get(0).get(0);
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Parses item as an enchant
     */
    private void checkEnchant() {
        if (enchantMods.size() < 1) {
            discard = true;
            return;
        }

        parentCategory = "enchantments";
        icon = Config.enchantment_icon;
        typeLine = null;

        // Match any negative or positive integer or double
        name = enchantMods.get(0).replaceAll("[-]?\\d*\\.?\\d+", "#");

        // "#% chance to Dodge Spell Damage if you've taken Spell Damage Recently" contains a newline in the middle
        if (name.contains("\n")) name = name.replace("\n", " ");

        // Var contains the enchant value (e.g "var:1-160" or "var:120")
        String numString = enchantMods.get(0).replaceAll("[^-.0-9]+", " ").trim();
        String[] numArray = numString.split(" ");
        findEnchantRolls(numArray);
        String numbers = String.join("-", numArray);

        if (!numbers.equals("")) variation = numbers;
    }

    /**
     * Determines the tier/roll of an enchant if it has mod tiers
     *
     * @param numArray List of numbers found in enchant
     */
    private void findEnchantRolls(String[] numArray) {
        // Assume name variable has the enchant name with numbers replaced by pound signs
        switch (name) {
            case "Lacerate deals # to # added Physical Damage against Bleeding Enemies":
                int num1 = Integer.parseInt(numArray[0]);
                int num2 = Integer.parseInt(numArray[1]);

                // Merc: (4-8) to (10-15)
                if (num1 <= 8 && num2 <= 15) {
                    numArray[0] = "8";
                    numArray[1] = "15";
                }
                // Uber: (14-18) to (20-25)
                else if (num1 >= 14 && num1 <= 18 && num2 <= 25 && num2 >= 20) {
                    numArray[0] = "18";
                    numArray[1] = "25";
                }

                break;
        }
    }

    /**
     * Contains some basic rules for currency
     */
    private void checkCurrency() {
        switch (name) {
            case "Chaos Orb":
            case "Imprint":
            case "Scroll Fragment":
            case "Alteration Shard":
            case "Binding Shard":
            case "Horizon Shard":
            case "Engineer's Shard":
            case "Chaos Shard":
            case "Regal Shard":
            case "Alchemy Shard":
            case "Transmutation Shard":
            case "Bestiary Orb":
            case "Necromancy Net":
            case "Thaumaturgical Net":
            case "Reinforced Steel Net":
            case "Strong Steel Net":
            case "Simple Steel Net":
            case "Reinforced Iron Net":
            case "Strong Iron Net":
            case "Simple Iron Net":
            case "Reinforced Rope Net":
            case "Strong Rope Net":
            case "Simple Rope Net":
            case "Unshaping Orb":
            case "Master Cartographer's Seal":
            case "Journeyman Cartographer's Seal":
            case "Apprentice Cartographer's Seal":
                discard = true;
                break;
        }
    }

    //------------------------------------------------------------------------------------------------------------
    // Getters and setters
    //------------------------------------------------------------------------------------------------------------

    public double getPrice() {
        return price;
    }

    public String getLevel() {
        return level;
    }

    public String getLinks() {
        return links;
    }

    public String getQuality() {
        return quality;
    }

    public String getChildCategory() {
        return childCategory;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public String getTier() {
        return tier;
    }

    public String getVariation() {
        return variation;
    }

    public boolean isDoNotIndex() {
        return doNotIndex;
    }

    public boolean isDiscard() {
        return discard;
    }

    public String getKey() {
        return key;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return typeLine;
    }

    public String isCorrupted() {
        return frameType == 4 ? (corrupted != null && corrupted ? "1" : "0") : null;
    }

    public int getFrame() {
        return frameType;
    }

    public String getId() {
        return id;
    }

    public String getLeague() {
        return league;
    }
}
