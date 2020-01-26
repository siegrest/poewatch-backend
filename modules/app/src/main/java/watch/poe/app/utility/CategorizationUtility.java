package watch.poe.app.utility;

import org.apache.commons.lang3.StringUtils;
import watch.poe.app.dto.river.ItemDto;

import java.util.List;

public class CategorizationUtility {

  /**
   * Find icon cdn category. For example, if the icon path is
   * "http://web.poecdn.com/image/Art/2DItems/Armours/Helmets/HarbingerShards/Shard1.png"
   * then the icon category is "HarbingerShards"
   *
   * @return Extracted category
   */
  public static String findIconCategory(ItemDto itemDto) {
    var splitItemType = itemDto.getIcon().split("/");
    return splitItemType[splitItemType.length - 2].toLowerCase();
  }

  public static String findIconName(ItemDto itemDto) {
    var slashPos = itemDto.getIcon().lastIndexOf('/');
    var dotPos = itemDto.getIcon().lastIndexOf(".png");
    return itemDto.getIcon().substring(slashPos + 1, dotPos);
  }

  public static String getFirstApiGroup(ItemDto itemDto) {
    var subCategories = itemDto.getExtended().getSubcategories();

    if (subCategories != null && !subCategories.isEmpty()) {
      return subCategories.get(0).toLowerCase();
    }

    return null;
  }

  public static List<String> getApiGroups(ItemDto itemDto) {
    if (itemDto == null || itemDto.getExtended() == null || itemDto.getExtended().getSubcategories() == null) {
      return List.of();
    }

    return itemDto.getExtended().getSubcategories();
  }

  public static String replacePrefix(String prefix, String name) {
    if (name == null || StringUtils.isBlank(name)) {
      return name;
    }

    // "Superior Ashen Wood Map" -> "Ashen Wood Map"
    if (name.startsWith(prefix)) {
      return name.substring(prefix.length());
    }

    return name;
  }

  public static boolean hasQuality(ItemDto itemDto) {
    if (itemDto.getProperties() == null) {
      return false;
    }

    var qProperty = itemDto.getProperties().stream()
      .filter(p -> "Quality".equals(p.getName()))
      .findFirst();

    return qProperty.isPresent();
  }

}
