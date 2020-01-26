package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import watch.poe.app.domain.*;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.PropertyDto;
import watch.poe.app.dto.river.SocketDto;
import watch.poe.app.exception.InvalidIconException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;

import java.util.Objects;

@Slf4j
public final class ItemUtility {

  public static boolean hasInfluence(ItemDto itemDto) {
    var influences = itemDto.getInfluences();
    return influences != null
      && (influences.getShaper() != null && influences.getShaper()
      || influences.getElder() != null && influences.getElder()
      || influences.getCrusader() != null && influences.getCrusader()
      || influences.getRedeemer() != null && influences.getRedeemer()
      || influences.getHunter() != null && influences.getHunter()
      || influences.getWarlord() != null && influences.getWarlord());
  }

  public static boolean isComplex(ItemDto itemDto, CategoryDto categoryDto) {
    var frameType = itemDto.getFrameType();
    return (frameType == Rarity.Magic || frameType == Rarity.Rare)
      && (categoryDto == CategoryDto.ACCESSORY
      || categoryDto == CategoryDto.ARMOUR
      || categoryDto == CategoryDto.JEWEL
      || categoryDto == CategoryDto.MAP
      || categoryDto == CategoryDto.FLASK
      || categoryDto == CategoryDto.CRAFTING_BASE
      || categoryDto == CategoryDto.ALTART
      || categoryDto == CategoryDto.WEAPON);
  }

  public static boolean isCraftable(ItemDto itemDto) {
    var frameType = itemDto.getFrameType();
    return frameType == Rarity.Normal || frameType == Rarity.Magic || frameType == Rarity.Rare;
  }

  public static boolean isSpecialSupportGem(ItemDto itemDto) {
    return itemDto.getTypeLine().equals("Empower Support")
      || itemDto.getTypeLine().equals("Enlighten Support")
      || itemDto.getTypeLine().equals("Enhance Support");
  }

  public static boolean isStackable(ItemDto itemDto) {
    return itemDto.getStackSize() != null && itemDto.getProperties() != null;
  }

  public static boolean isLinkable(ItemWrapper wrapper) {
    var categoryDto = wrapper.getCategoryDto();
    var groupDto = wrapper.getGroupDto();
    var itemDto = wrapper.getItemDto();
    return (CategoryDto.WEAPON == categoryDto
      || CategoryDto.ARMOUR == categoryDto)
      && (GroupDto.CHEST == groupDto
      || GroupDto.STAFF == groupDto
      || GroupDto.TWO_HAND_SWORD == groupDto
      || GroupDto.TWO_HAND_MACE == groupDto
      || GroupDto.TWO_HAND_AXE == groupDto
      || GroupDto.BOW == groupDto
      || GroupDto.WARSTAFF == groupDto)
      && itemDto.getSockets() != null;
  }

  public static boolean isUnique(ItemDto itemDto) {
    return itemDto.getFrameType() == Rarity.Unique || itemDto.getFrameType() == Rarity.Relic;
  }

  public static boolean isLabEnchantment(ItemDto itemDto) {
    // todo: double check the logic here
    var firstGroup = CategorizationUtility.getFirstApiGroup(itemDto);
    return itemDto.getEnchantMods() != null
      && "armour".equals(itemDto.getExtended().getCategory())
      && ("helmets".equals(firstGroup) || "gloves".equals(firstGroup) || "boots".equals(firstGroup));
  }

  // todo: move to CategorizationUtility
  public static boolean isAbyssalJewel(ItemDto itemDto) {
    return itemDto.getAbyssJewel() != null && itemDto.getAbyssJewel();
  }

  public static boolean isCorrupted(ItemDto itemDto) {
    return itemDto.getIsCorrupted() != null && itemDto.getIsCorrupted();
  }

  public static boolean isAltArt(ItemWrapper wrapper) {
    return wrapper.getItemDto().getRaceReward() != null;
  }

  /**
   * Removes any unnecessary fields from the item's icon
   *
   * @param icon Item's icon
   * @return Formatted icon URL
   */
  public static String formatIcon(String icon) throws InvalidIconException {
    if (icon == null) {
      throw new InvalidIconException("Icon was null");
    }

    var splitURL = icon.split("\\?", 2);
    var fullIcon = splitURL[0];

    if (splitURL.length > 1) {
      var paramBuilder = new StringBuilder();

      for (String param : splitURL[1].split("&")) {
        var splitParam = param.split("=");

        switch (splitParam[0]) {
          case "scale": // scale
          case "w": // width
          case "h": // height
          case "mr": // shaped
          case "mn": // series
          case "mt": // tier
          case "mb": // blighted
          case "relic":
            paramBuilder.append("&");
            paramBuilder.append(splitParam[0]);
            paramBuilder.append("=");
            paramBuilder.append(splitParam[1]);
            break;
          case "v":
          case "mg": // http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/SulphurVents.png?scale=1&w=1&h=1&mn=6&mt=15&mg=4&v=a3a02754d0bd1489b7f19658c1746e3d
          case "duplicated":
          case "synthesised":
          case "fractured":
            break;
          default:
            log.warn("Unhandled icon parameter '{}' in '{}'", splitParam[0], icon);
            break;
        }
      }

      // If there are parameters that should be kept, add them to fullIcon
      if (paramBuilder.length() > 0) {
        // Replace the first "&" symbol with "?"
        paramBuilder.setCharAt(0, '?');
        fullIcon += paramBuilder.toString();
      }
    }

    return fullIcon;
  }

  public static Integer extractMapTier(ItemDto itemDto) throws ItemParseException {
    if (itemDto.getProperties() != null) {
      for (PropertyDto prop : itemDto.getProperties()) {
        if (!"Map Tier".equals(prop.getName()) || prop.getValues().isEmpty()) {
          continue;
        } else if (prop.getValues().get(0).isEmpty()) {
          break;
        }

        return Integer.parseInt(prop.getValues().get(0).get(0));
      }
    }

    throw new ItemParseException(DiscardBasis.MAP_TIER_MISSING);
  }

  public static Integer extractMapSeries(ItemDto itemDto) throws ItemParseException {
    /* Currently the series are as such:
     http://web.poecdn.com/image/Art/2DItems/Maps/Map45.png?scale=1&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map76.png?scale=1&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/AtlasMaps/Chimera.png?scale=1&scaleIndex=0&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=1&mt=0
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=2&mt=0
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=3&mt=0
    */

    var iconCategory = CategorizationUtility.findIconCategory(itemDto);
    var seriesNumber = 0;

    // Attempt to find series number for newer maps
    try {
      var iconParams = itemDto.getIcon().split("\\?", 2)[1].split("&");
      for (var param : iconParams) {
        var splitParam = param.split("=");
        if (splitParam[0].equals("mn")) {
          seriesNumber = Integer.parseInt(splitParam[1]);
          break;
        }
      }
    } catch (Exception ex) {
      throw new ItemParseException(DiscardBasis.PARSE_MAP_SERIES_FAILED);
    }

    if (iconCategory.equalsIgnoreCase("Maps")) {
      return 0;
    } else if (iconCategory.equalsIgnoreCase("act4maps")) {
      return 1;
    } else if (iconCategory.equalsIgnoreCase("AtlasMaps")) {
      return 2;
    } else if (iconCategory.equalsIgnoreCase("New") && seriesNumber > 0) {
      return seriesNumber + 2;
    } else {
      throw new ItemParseException(DiscardBasis.INVALID_MAP_SERIES);
    }
  }

  public static Integer extractLinks(ItemDto itemDto) throws ItemParseException {
    if (itemDto.getSockets() == null) {
      throw new ItemParseException(DiscardBasis.NO_SOCKETS);
    }

    // Group links together
    Integer[] linkArray = new Integer[]{0, 0, 0, 0, 0, 0};
    for (SocketDto socket : itemDto.getSockets()) {
      linkArray[socket.getGroup()]++;
    }

    // Find largest single link
    int largestLink = 0;
    for (Integer link : linkArray) {
      if (link > largestLink) {
        largestLink = link;
      }
    }

    return largestLink > 4 ? largestLink : null;
  }

  public static Integer extractMaxStackSize(ItemDto itemDto) throws ItemParseException {
    if (itemDto.getStackSize() == null || itemDto.getProperties() == null) {
      return null;
    }

        /*
        This is what it looks like as JSON:
            "properties": [{
                "name": "Stack Size",
                "values": [["42/1000", 0]],
                "displayMode": 0
              }
            ]
         */

    // Find first stacks size property
    var oProperty = itemDto.getProperties().stream()
      .filter(i -> "Stack Size".equals(i.getName()))
      .findFirst();
    if (oProperty.isEmpty()) {
      // todo: might be unintended
      throw new ItemParseException(DiscardBasis.STACK_SIZE_MISSING);
    }

    var property = oProperty.get();
    if (property.getValues().isEmpty() || property.getValues().get(0).isEmpty()) {
      throw new ItemParseException(DiscardBasis.STACK_SIZE_MISSING);
    }

    var stackSizeString = property.getValues().get(0).get(0);
    var index = stackSizeString.indexOf("/");

    // Must contain the slash eg "42/1000"
    if (index < 0) {
      throw new ItemParseException(DiscardBasis.STACK_SIZE_SLASH_MISSING);
    }

    try {
      return Integer.parseInt(stackSizeString.substring(index + 1));
    } catch (NumberFormatException ex) {
      throw new ItemParseException(DiscardBasis.PARSE_STACK_SIZE);
    }
  }

  public static Integer extractGemLevel(ItemDto itemDto) {
    for (var prop : itemDto.getProperties()) {
      if ("Level".equals(prop.getName())) {
        return Integer.parseInt(prop.getValues().get(0).get(0).split(" ")[0]);
      }
    }

    return null;
  }

  public static Integer extractGemQuality(ItemDto itemDto) {
    for (var prop : itemDto.getProperties()) {
      if ("Quality".equals(prop.getName())) {
        return Integer.parseInt(prop.getValues().get(0).get(0)
          .replace("+", "")
          .replace("%", ""));
      }
    }

    return null;
  }

  public static boolean itemEquals(Item item1, Item item2) {
    if (item1 == item2) {
      return true;
    }

    return itemBaseEquals(item1.getBase(), item2.getBase()) &&
      Objects.equals(item1.getVariation(), item2.getVariation()) &&
      Objects.equals(item1.getStackSize(), item2.getStackSize()) &&
      Objects.equals(item1.getItemLevel(), item2.getItemLevel()) &&
      Objects.equals(item1.getLinks(), item2.getLinks()) &&
      Objects.equals(item1.getMapTier(), item2.getMapTier()) &&
      Objects.equals(item1.getMapSeries(), item2.getMapSeries()) &&
      Objects.equals(item1.getGemLevel(), item2.getGemLevel()) &&
      Objects.equals(item1.getGemQuality(), item2.getGemQuality()) &&
      Objects.equals(item1.getCorrupted(), item2.getCorrupted()) &&
      Objects.equals(item1.getInfluenceShaper(), item2.getInfluenceShaper()) &&
      Objects.equals(item1.getInfluenceElder(), item2.getInfluenceElder()) &&
      Objects.equals(item1.getInfluenceCrusader(), item2.getInfluenceCrusader()) &&
      Objects.equals(item1.getInfluenceRedeemer(), item2.getInfluenceRedeemer()) &&
      Objects.equals(item1.getInfluenceHunter(), item2.getInfluenceHunter()) &&
      Objects.equals(item1.getInfluenceWarlord(), item2.getInfluenceWarlord()) &&
      Objects.equals(item1.getEnchantMin(), item2.getEnchantMin()) &&
      Objects.equals(item1.getEnchantMax(), item2.getEnchantMax());
  }

  public static boolean itemBaseEquals(ItemBase base1, ItemBase base2) {
    if (base1 == base2) {
      return true;
    }

    if (base1 == null && base2 != null || base1 != null && base2 == null) {
      return false;
    }

    return base1.getCategory().equals(base2.getCategory()) &&
      base1.getGroup().equals(base2.getGroup()) &&
      Objects.equals(base1.getName(), base2.getName()) &&
      Objects.equals(base1.getBaseType(), base2.getBaseType()) &&
      base1.getFrameType().equals(base2.getFrameType());
  }

  public static String extractEnchantmentName(ItemDto itemDto) {
    var enchantLine = itemDto.getEnchantMods().get(0);

    // Match any negative or positive integer or double
    var enchantName = enchantLine.replaceAll("[-]?\\d*\\.?\\d+", "#");

    // "#% chance to Dodge Spell Damage if you've taken Spell Damage Recently" contains a newline in the middle
    if (enchantName.contains("\n")) {
      return enchantName.replace("\n", " ");
    } else {
      return enchantName;
    }
  }

  public static Double[] extractEnchantmentRolls(ItemDto itemDto) throws ItemParseException {
    String numString = itemDto.getEnchantMods().get(0).replaceAll("[^-.0-9]+", " ").trim();
    if (StringUtils.isBlank(numString)) {
      return null;
    }

    String[] numArray = numString.split(" ");

    if (numArray.length == 1) {
      return new Double[]{
        Double.parseDouble(numArray[0]),
        Double.parseDouble(numArray[0])
      };
    } else if (numArray.length == 2) {
      return new Double[]{
        Double.parseDouble(numArray[0]),
        Double.parseDouble(numArray[1])
      };
    }

    throw new ItemParseException(ParseExceptionBasis.INVALID_ENCHANTMENT_ROLLS);
  }

  public static String extractIconName(String icon) {
    var pos1 = icon.lastIndexOf('/');
    var pos2 = icon.lastIndexOf(".png");
    return icon.substring(pos1 + 1, pos2);
  }

}
