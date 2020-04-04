package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.GroupDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.PropertyDto;
import watch.poe.app.dto.river.SocketDto;
import watch.poe.app.dto.wrapper.ItemWrapper;
import watch.poe.app.exception.InvalidIconException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.code.ParseErrorCode;
import watch.poe.persistence.model.item.FrameType;
import watch.poe.persistence.model.item.ItemBase;
import watch.poe.persistence.model.item.ItemDetail;

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

  public static boolean isCraftable(ItemDto itemDto) {
    return FrameType.NORMAL.is(itemDto.getFrameType())
      || FrameType.MAGIC.is(itemDto.getFrameType())
      || FrameType.RARE.is(itemDto.getFrameType());
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
    return FrameType.UNIQUE.is(itemDto.getFrameType()) || FrameType.RELIC.is(itemDto.getFrameType());
  }

  public static boolean isCorrupted(ItemDto itemDto) {
    return itemDto.getCorrupted() != null && itemDto.getCorrupted();
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

    throw new ItemParseException(DiscardErrorCode.MAP_TIER_MISSING);
  }

  public static Integer extractLinks(ItemDto itemDto) throws ItemParseException {
    if (itemDto.getSockets() == null) {
      throw new ItemParseException(DiscardErrorCode.NO_SOCKETS);
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
      throw new ItemParseException(DiscardErrorCode.STACK_SIZE_MISSING);
    }

    var property = oProperty.get();
    if (property.getValues().isEmpty() || property.getValues().get(0).isEmpty()) {
      throw new ItemParseException(DiscardErrorCode.STACK_SIZE_MISSING);
    }

    var stackSizeString = property.getValues().get(0).get(0);
    var index = stackSizeString.indexOf("/");

    // Must contain the slash eg "42/1000"
    if (index < 0) {
      throw new ItemParseException(DiscardErrorCode.STACK_SIZE_SLASH_MISSING);
    }

    try {
      return Integer.parseInt(stackSizeString.substring(index + 1));
    } catch (NumberFormatException ex) {
      throw new ItemParseException(DiscardErrorCode.PARSE_STACK_SIZE);
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

  public static boolean itemEquals(ItemDetail itemDetail1, ItemDetail itemDetail2) {
    if (itemDetail1 == itemDetail2) {
      return true;
    }

    if (itemDetail1 == null || itemDetail2 == null) {
      return false;
    }

    return itemBaseEquals(itemDetail1.getBase(), itemDetail2.getBase()) &&
      Objects.equals(itemDetail1.getVariation(), itemDetail2.getVariation()) &&
      Objects.equals(itemDetail1.getStackSize(), itemDetail2.getStackSize()) &&
      Objects.equals(itemDetail1.getItemLevel(), itemDetail2.getItemLevel()) &&
      Objects.equals(itemDetail1.getLinks(), itemDetail2.getLinks()) &&
      Objects.equals(itemDetail1.getMapTier(), itemDetail2.getMapTier()) &&
      Objects.equals(itemDetail1.getMapSeries(), itemDetail2.getMapSeries()) &&
      Objects.equals(itemDetail1.getGemLevel(), itemDetail2.getGemLevel()) &&
      Objects.equals(itemDetail1.getGemQuality(), itemDetail2.getGemQuality()) &&
      Objects.equals(itemDetail1.getCorrupted(), itemDetail2.getCorrupted()) &&
      Objects.equals(itemDetail1.getInfluenceShaper(), itemDetail2.getInfluenceShaper()) &&
      Objects.equals(itemDetail1.getInfluenceElder(), itemDetail2.getInfluenceElder()) &&
      Objects.equals(itemDetail1.getInfluenceCrusader(), itemDetail2.getInfluenceCrusader()) &&
      Objects.equals(itemDetail1.getInfluenceRedeemer(), itemDetail2.getInfluenceRedeemer()) &&
      Objects.equals(itemDetail1.getInfluenceHunter(), itemDetail2.getInfluenceHunter()) &&
      Objects.equals(itemDetail1.getInfluenceWarlord(), itemDetail2.getInfluenceWarlord()) &&
      Objects.equals(itemDetail1.getEnchantMin(), itemDetail2.getEnchantMin()) &&
      Objects.equals(itemDetail1.getEnchantMax(), itemDetail2.getEnchantMax());
  }

  public static boolean itemBaseEquals(ItemBase base1, ItemBase base2) {
    if (base1 == base2) {
      return true;
    }

    if (base1 == null || base2 == null) {
      return false;
    }

    return StringUtils.equals(base1.getCategory().getName(), base2.getCategory().getName())
      && StringUtils.equals(base1.getGroup().getName(), base2.getGroup().getName())
      && StringUtils.equals(base1.getName(), base2.getName())
      && StringUtils.equals(base1.getBaseType(), base2.getBaseType())
      && base1.getFrameType().equals(base2.getFrameType());
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

    throw new ItemParseException(ParseErrorCode.INVALID_ENCHANTMENT_ROLLS);
  }

  public static String extractIconName(String icon) {
    var pos1 = icon.lastIndexOf('/');
    var pos2 = icon.lastIndexOf(".png");
    return icon.substring(pos1 + 1, pos2);
  }

}
