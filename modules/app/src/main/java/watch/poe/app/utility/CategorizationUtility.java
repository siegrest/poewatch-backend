package watch.poe.app.utility;

import watch.poe.app.domain.wrapper.CategoryWrapper;
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

  public static boolean isBreachSplinter(CategoryWrapper wrapper) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Breach/BreachShardPhysical.png?scale=1&w=1&h=1&v=25f31f4a5e1ba4540cb7bfa03b82c1e8, league=Standard, id=d541e401a84c88873096ab6f78601d37ca7a6c27a08cb120e091bcd46ce06d3c, name=, typeLine=Splinter of Uul-Netol, note=null, stackSize=105, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[105/100, 0]])], sockets=null, explicitMods=null, enchantMods=null)
    return "breach".equals(wrapper.getIconCategory())
      && wrapper.getItemDto().getTypeLine() != null
      && wrapper.getItemDto().getTypeLine().startsWith("Splinter of ");
  }

  public static boolean isTimelessSplinter(CategoryWrapper wrapper) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/KaruiShard.png?scale=1&w=1&h=1&v=e3ab494b7e170292856cd88874110b61, league=Standard, id=1158619105693a404deb192024797c33beb04b66b99193b3f85ab8738946f8bd, name=, typeLine=Timeless Karui Splinter, note=null, stackSize=6, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[6/100, 0]])], sockets=null, explicitMods=null, enchantMods=null)
    return "maps".equals(wrapper.getIconCategory())
      && wrapper.getItemDto().getTypeLine() != null
      && wrapper.getItemDto().getTypeLine().startsWith("Timeless ")
      && wrapper.getItemDto().getTypeLine().endsWith(" Splinter");
  }

  public static boolean isScarab(CategoryWrapper wrapper) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Normal, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Scarabs/LesserScarabBreach.png?scale=1&w=1&h=1&v=602060cab574718c45fd3026b2c059c3, league=Standard, id=df5f926805dd079d08efc4606357c533cda14070a320f034643a06bef32219dc, name=, typeLine=Rusted Breach Scarab, note=~price 4 chaos, stackSize=null, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=[fragment, scarab], prefixes=null, suffixes=null), properties=null, sockets=null, explicitMods=[Area contains an additional Breach], enchantMods=null)
    return getApiGroups(wrapper.getItemDto()).contains("scarab");
  }

}
