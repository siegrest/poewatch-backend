package watch.poe.app.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.GroupDto;
import watch.poe.app.dto.wrapper.ItemWrapper;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.MapIconService;
import watch.poe.app.service.resource.CorruptedItemService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemTypeUtility;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.item.FrameType;
import watch.poe.persistence.model.item.ItemDetail;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ItemDetailParserService {

  private static final String ENCHANTMENT_ICON = "http://web.poecdn.com/image/Art/2DItems/Currency/Enchantment.png?scale=1&w=1&h=1";

  private final ItemVariantService itemVariantService;
  private final CorruptedItemService corruptedItemService;
  private final ItemCategorizationService categorizationService;
  private final ItemGroupingService groupingService;
  private final ItemBaseParserService itemBaseParserService;
  private final MapIconService mapIconService;

  public ItemDetail parse(ItemWrapper wrapper) throws ItemParseException, GroupingException {
    var itemDto = wrapper.getItemDto();

    var categoryDto = categorizationService.parseCategoryDto(wrapper);
    wrapper.setCategoryDto(categoryDto);

    var groupDto = groupingService.parseGroupDto(wrapper);
    wrapper.setGroupDto(groupDto);

    var base = itemBaseParserService.parse(categoryDto, groupDto, itemDto);
    wrapper.getItemDetail().setBase(base);

    parseIcon(wrapper);
    parseCorrupted(wrapper);

    if (wrapper.getCategoryDto() == CategoryDto.MAP) {
      parseMap(wrapper);
    }

    if (wrapper.getCategoryDto() == CategoryDto.GEM) {
      parseGem(wrapper);
    }

    if (ItemUtility.isStackable(itemDto)) {
      parseStackSize(wrapper);
    }

    if (ItemUtility.isLinkable(wrapper)) {
      parseLinks(wrapper);
    }

    if (itemVariantService.hasVariation(wrapper.getItemDto())) {
      parseVariant(wrapper);
    }

    if (ItemTypeUtility.isLabEnchantment(wrapper.getItemDto())) {
      parseEnchantment(wrapper);
    }

    if (ItemTypeUtility.isAltArt(wrapper)) {
      parseAltArt(wrapper);
    }

    return wrapper.getItemDetail();
  }

  public void parseIcon(ItemWrapper wrapper) throws ItemParseException {
    var icon = wrapper.getItemDto().getIcon();
    var newIcon = ItemUtility.formatIcon(icon);
    wrapper.getItemDetail().setIcon(newIcon);
  }

  public void parseMap(ItemWrapper wrapper) throws ItemParseException {
    var base = wrapper.getItemDetail().getBase();
    var item = wrapper.getItemDetail();
    var itemDto = wrapper.getItemDto();

    if (wrapper.getCategoryDto() == CategoryDto.MAP && FrameType.MAGIC.is(itemDto.getFrameType())) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Magic, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map66.png?scale=1&w=1&h=1&v=3557e95be294ee38dce858022f33f406, league=Standard, id=e48eef62374f21e1f1feea5436893b45ec2690aea37534546e7a28b2782284a4, name=, typeLine=Titan's Geode Map of Power, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[3, 0]]), PropertyDto(name=Item Quantity, values=[[+24%, 1]]), PropertyDto(name=Item Rarity, values=[[+12%, 1]])], sockets=null, explicitMods=[Monsters gain 1 Power Charge every 20 seconds, Unique Boss has 25% increased Life, Unique Boss has 20% increased Area of Effect], enchantMods=null)
      wrapper.discard(DiscardErrorCode.PARSE_MAGIC_MAP);
      return;
    }
    if (wrapper.getCategoryDto() == CategoryDto.MAP && FrameType.RARE.is(itemDto.getFrameType())) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Rare, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map67.png?scale=1&w=1&h=1&v=7806f58352e76963ac2bdaf1e515ecf8, league=Standard, id=4f4e21f2e662d2f7e82a1fa543ef7a7923501f17f6334ede7873aa7e6dde50c9, name=Tranquil Shadows, typeLine=Arena Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]]), PropertyDto(name=Item Quantity, values=[[+62%, 1]]), PropertyDto(name=Item Rarity, values=[[+25%, 1]]), PropertyDto(name=Monster Pack Size, values=[[+10%, 1]]), PropertyDto(name=Quality, values=[[+12%, 1]])], sockets=null, explicitMods=[Area has patches of shocking ground, Players have Elemental Equilibrium, Monsters reflect 13% of Elemental Damage, +40% Monster Fire Resistance], enchantMods=null)
      // todo: actually we can
      wrapper.discard(DiscardErrorCode.PARSE_RARE_MAP);
      return;
    }

    if (wrapper.getCategoryDto() == CategoryDto.MAP) {
      var tier = ItemUtility.extractMapTier(itemDto);
      item.setMapTier(tier);

      var series = mapIconService.parseSeries(itemDto);
      item.setMapSeries(series);
    }

    if (wrapper.getGroupDto() != GroupDto.UNIQUE_MAP) {
      base.setFrameType(FrameType.NORMAL);
    }
  }

  public void parseGem(ItemWrapper wrapper) {
    var item = wrapper.getItemDetail();
    var itemDto = wrapper.getItemDto();

    var level = ItemUtility.extractGemLevel(itemDto);
    if (level == null) {
      wrapper.discard(DiscardErrorCode.GEM_LEVEL_MISSING);
      return;
    }

    var quality = ItemUtility.extractGemQuality(itemDto);
    if (quality == null) {
      wrapper.discard(DiscardErrorCode.GEM_QUALITY_MISSING);
      return;
    }

    // Accept some quality ranges
    if (quality < 5) {
      quality = 0;
    } else if (quality > 17 && quality < 23) {
      quality = 20;
    } else if (quality != 23) {
      wrapper.discard(DiscardErrorCode.GEM_QUALITY_OUT_OF_RANGE);
      return;
    }

    // Begin the long block that filters out gems based on a number of properties
    if (ItemTypeUtility.isSpecialSupportGem(itemDto)) {
      // Quality doesn't matter for lvl 3 and 4
      if (level > 2) {
        quality = 0;
      }
    } else if (itemDto.getTypeLine().equals("Brand Recall")) {
      if (level <= 2) {
        level = 1;
      } else if (level < 5) {
        wrapper.discard(DiscardErrorCode.GEM_BRAND_RECALL_LEVEL_OUT_OF_RANGE);
        return;
      }
    } else {
      // Accept some level ranges
      if (level < 5) {
        level = 1;
      } else if (level < 20) {
        wrapper.discard(DiscardErrorCode.GEM_LEVEL_OUT_OF_RANGE);
        return;
      }
    }

    if (itemDto.getCorrupted() != null && !itemDto.getCorrupted() && (level > 20 || quality > 20)) {
      wrapper.discard(DiscardErrorCode.GEM_API_BUG);
      return;
    }

    item.setGemLevel(level);
    item.setGemQuality(quality);
  }

  public void parseStackSize(ItemWrapper wrapper) throws ItemParseException {
    var stackSize = ItemUtility.extractMaxStackSize(wrapper.getItemDto());
    wrapper.getItemDetail().setStackSize(stackSize);
  }

  public void parseVariant(ItemWrapper wrapper) {
    var item = wrapper.getItemDetail();
    var itemDto = wrapper.getItemDto();

    var variant = itemVariantService.getVariation(itemDto);
    if (variant.isEmpty()) {
      return;
    }

    item.setVariation(variant.get().getVariation());
  }

  public void parseCorrupted(ItemWrapper wrapper) {
    var categoryDto = wrapper.getCategoryDto();
    var item = wrapper.getItemDetail();
    var itemDto = wrapper.getItemDto();

    if (categoryDto == CategoryDto.GEM) {
      item.setCorrupted(itemDto.getCorrupted());
      return;
    }

    // todo: leaguestones, talismans, breach rings

    if (ItemUtility.isUnique(itemDto)) {
      if (corruptedItemService.isCorrupted(itemDto.getName())) {
        item.setCorrupted(itemDto.getCorrupted());
      }
    }
  }

  public void parseEnchantment(ItemWrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var item = wrapper.getItemDetail();

    var enchantName = ItemUtility.extractEnchantmentName(itemDto);
    var rolls = ItemUtility.extractEnchantmentRolls(itemDto);

    item.setIcon(ENCHANTMENT_ICON);

    if (rolls != null) {
      flattenEnchantRolls(enchantName, rolls);
      item.setEnchantMin(rolls[0]);
      item.setEnchantMax(rolls[1]);
    }
  }

  private void flattenEnchantRolls(String enchantName, Double[] rolls) {
    // Assume name variable has the enchant name with numbers replaced by pound signs
    switch (enchantName) {
      case "Lacerate deals # to # added Physical Damage against Bleeding Enemies":
        // Merc: (4-8) to (10-15)
        if (rolls[0] <= 8 && rolls[1] <= 15) {
          rolls[0] = 8d;
          rolls[1] = 15d;
        }
        // Uber: (14-18) to (20-25)
        else if (rolls[0] >= 14 && rolls[0] <= 18 && rolls[1] <= 25 && rolls[1] >= 20) {
          rolls[0] = 18d;
          rolls[1] = 25d;
        }

        break;
    }
  }

  private void parseAltArt(ItemWrapper wrapper) {
    var itemDto = wrapper.getItemDto();
    var base = wrapper.getItemDetail().getBase();

    var iconName = ItemUtility.extractIconName(wrapper.getItemDto().getIcon());
    wrapper.getItemDetail().setVariation(iconName);

    if (FrameType.MAGIC.is(itemDto.getFrameType()) || FrameType.RARE.is(itemDto.getFrameType())) {
      base.setFrameType(FrameType.NORMAL);
    }
  }

  private void parseLinks(ItemWrapper wrapper) throws ItemParseException {
    var links = ItemUtility.extractLinks(wrapper.getItemDto());
    wrapper.getItemDetail().setLinks(links);
  }

}
