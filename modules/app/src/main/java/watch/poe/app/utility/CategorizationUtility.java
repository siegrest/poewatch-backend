package watch.poe.app.utility;

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

  public static boolean isBreachSplinter(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Breach/BreachShardPhysical.png?scale=1&w=1&h=1&v=25f31f4a5e1ba4540cb7bfa03b82c1e8, league=Standard, id=d541e401a84c88873096ab6f78601d37ca7a6c27a08cb120e091bcd46ce06d3c, name=, typeLine=Splinter of Uul-Netol, note=null, stackSize=105, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[105/100, 0]])], sockets=null, explicitMods=null, enchantMods=null)
    return "breach".equals(findIconCategory(itemDto))
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Splinter of ");
  }

  public static boolean isTimelessSplinter(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/KaruiShard.png?scale=1&w=1&h=1&v=e3ab494b7e170292856cd88874110b61, league=Standard, id=1158619105693a404deb192024797c33beb04b66b99193b3f85ab8738946f8bd, name=, typeLine=Timeless Karui Splinter, note=null, stackSize=6, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[6/100, 0]])], sockets=null, explicitMods=null, enchantMods=null)
    return "maps".equals(findIconCategory(itemDto))
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Timeless ")
      && itemDto.getTypeLine().endsWith(" Splinter");
  }

  public static boolean isScarab(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Normal, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Scarabs/LesserScarabBreach.png?scale=1&w=1&h=1&v=602060cab574718c45fd3026b2c059c3, league=Standard, id=df5f926805dd079d08efc4606357c533cda14070a320f034643a06bef32219dc, name=, typeLine=Rusted Breach Scarab, note=~price 4 chaos, stackSize=null, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=[fragment, scarab], prefixes=null, suffixes=null), properties=null, sockets=null, explicitMods=[Area contains an additional Breach], enchantMods=null)
    return getApiGroups(itemDto).contains("scarab");
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

  public static boolean isTimelessEmblem(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Timeless ")
      && itemDto.getTypeLine().endsWith(" Emblem");
  }

  public static boolean isSacrificeFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Sacrifice at ");
  }

  public static boolean isBreachstone(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().endsWith(" Breachstone");
  }

  public static boolean isMortalFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Mortal ");
  }

  public static boolean isDivineVessel(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().equals("Divine Vessel");
  }

  public static boolean isShaperGuardianFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Fragment of the ");
  }

  public static boolean isElderGuardianFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && !itemDto.getTypeLine().startsWith("Fragment of the ")
      && itemDto.getTypeLine().startsWith("Fragment of ")
      && findIconName(itemDto).startsWith("AtlasMapGuardian");
  }

  public static boolean isUberElderFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Fragment of ")
      && findIconName(itemDto).startsWith("UberElder");
  }

  public static boolean isOfferingGoddess(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().equals("Offering to the Goddess");
  }

  public static boolean isPaleCourtFrag(ItemDto itemDto) {
    return getApiGroups(itemDto).contains("fragment")
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().endsWith(" Key")
      && CategorizationUtility.findIconName(itemDto).contains("PaleCourt");
  }

  public static boolean isNetCurrency(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/BestiaryTrap8.png?scale=1&w=1&h=1&v=1bc59150f49d6e2c882c3496cffdfbe0, league=Standard, id=1ccdba33dc1fb49ca2ae1ce2f89114fd428d090d06cdddec3115bcd5e8093f4a, name=, typeLine=Reinforced Steel Net, note=null, stackSize=38, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[38/100, 0]]), PropertyDto(name=Net Tier, values=[[8, 0]])], sockets=null, explicitMods=[Effective against Beasts of levels 60 to 75.
    return "currency".equals(itemDto.getExtended().getCategory())
      && itemDto.getProperties() != null
      && itemDto.getProperties().stream().anyMatch(p -> "Net Tier".equals(p.getName()));
  }

  public static boolean isVialCurrency(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/VialCowardsChains.png?scale=1&w=1&h=1&v=d4db2a42dab6d3100560db88cc156762, league=Standard, id=9cf83469b33ff13bfc71332b15a2f3e7346b7486fcc9dd1f5066a23be954cbc4, name=, typeLine=Vial of Consequence, note=null, stackSize=1, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[1/10, 0]])], sockets=null, explicitMods=null, enchantMods=null)
    return "currency".equals(itemDto.getExtended().getCategory())
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Vial of ");
  }

  public static boolean isBreachBlessing(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Currency, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Breach/BreachUpgraderPhysical.png?scale=1&w=1&h=1&v=cf3d245db6b6bb796112a952d74543e0, league=Standard, id=2e96b2c71db081340221d255121ea69a2f4cfd1522a7579796cc6f2fe5cd208d, name=, typeLine=Blessing of Uul-Netol, note=null, stackSize=5, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=currency, subcategories=null, prefixes=null, suffixes=null), properties=[PropertyDto(name=Stack Size, values=[[5/10, 0]])], sockets=null, explicitMods=[Upgrades a breach unique item to a more powerful version], enchantMods=null)
    return "currency".equals(itemDto.getExtended().getCategory())
      && itemDto.getTypeLine() != null
      && itemDto.getTypeLine().startsWith("Blessing of ");
  }

  public static boolean isWatchstone(ItemDto itemDto) {
    // ItemDto(isIdentified=true, itemLevel=0, frameType=Unique, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Currency/Strongholds/IvoryWatchstone5.png?scale=1&w=1&h=1&v=f9dbc175d87158a633ca2c12b1e395ba, league=Metamorph, id=4ef0794a4b787e39adbac89624e6a6d7cee4fd48ec01ba833f532748815acb4f, name=Territories Unknown, typeLine=Ivory Watchstone, note=null, stackSize=null, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=watchstones, subcategories=null, prefixes=null, suffixes=null), properties=null, sockets=null, explicitMods=[Map has 1 additional random Suffix, 18 uses remaining], enchantMods=null)
    return itemDto.getExtended().getCategory().equals("watchstones");
  }

  public static boolean isReliquaryKey(ItemDto itemDto) {
    //  ItemDto(isIdentified=true, itemLevel=0, frameType=Normal, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/VaultMap.png?scale=1&w=1&h=1&v=cb50511b7087323b10a19559bfb2be29, league=Standard, id=1e6bfa14344cf93a9697366578d3a9a79dd1796b224daa5dd6f2dd9368b9e9cf, name=, typeLine=Ancient Reliquary Key, note=null, stackSize=null, prophecyText=null, abyssJewel=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null, prefixes=null, suffixes=null), properties=null, sockets=null, explicitMods=null, enchantMods=null)
    return itemDto.getExtended().getCategory().equals("maps")
      && itemDto.getTypeLine() != null
      && (itemDto.getTypeLine().equals("Ancient Reliquary Key")
      || itemDto.getTypeLine().equals("Timeworn Reliquary Key"));
  }

}
