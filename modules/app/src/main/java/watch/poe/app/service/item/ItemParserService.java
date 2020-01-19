package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.*;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.resource.CorruptedItemService;
import watch.poe.app.service.resource.GroupMappingService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;

@Service
@Slf4j
public final class ItemParserService {

  @Autowired
  private ItemVariantService itemVariantService;
  @Autowired
  private GroupMappingService groupMappingService;
  @Autowired
  private CorruptedItemService corruptedItemService;

  public Item parse(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();

    parseCategoryDto(wrapper);
    parseGroupDto(wrapper);
    parseItemBase(wrapper);
    parseIcon(wrapper);
    parseCorrupted(wrapper);

    if (wrapper.getCategoryDto() == CategoryDto.map && (wrapper.getGroupDto() == GroupDto.map || wrapper.getGroupDto() == GroupDto.unique)) {
      parseMap(wrapper);
    }

    if (wrapper.getCategoryDto() == CategoryDto.gem) {
      parseGem(wrapper);
    }

    if (ItemUtility.isStackable(itemDto)) {
      parseStackSize(wrapper);
    }

    if (ItemUtility.isLinkable(wrapper)) {
      var links = ItemUtility.extractLinks(wrapper);
      wrapper.getItem().setLinks(links);
    }

    if (itemVariantService.hasVariation(wrapper.getItemDto())) {
      parseVariant(wrapper);
    }

    if (ItemUtility.isComplex(itemDto, wrapper.getCategoryDto())) {
      parseComplex(wrapper);
    }

    return wrapper.getItem();
  }

  public void parseIcon(Wrapper wrapper) throws ItemParseException {
    var icon = wrapper.getItemDto().getIcon();
    var newIcon = ItemUtility.formatIcon(icon);
    wrapper.getItem().setIcon(newIcon);
  }

  public void parseMap(Wrapper wrapper) throws ItemParseException {
    var base = wrapper.getItem().getBase();
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    if (wrapper.getGroupDto() == GroupDto.unique && !itemDto.isIdentified()) {
      // ItemDto(isIdentified=false, itemLevel=0, frameType=Unique, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/musicbox.png?scale=1&w=1&h=1&v=a8738647137a02c29c1b89d51d1bf58b, league=Standard, id=e703a5ae16defc94b606858fc4d53600be694ac8bed75f533b76f22ee90f03e3, name=, typeLine=Overgrown Shrine Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]])], sockets=null, explicitMods=null, enchantMods=null)
      // todo: actually we could
      wrapper.discard(DiscardBasis.PARSE_UNID_UNIQUE_MAP);
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Magic) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Magic, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map66.png?scale=1&w=1&h=1&v=3557e95be294ee38dce858022f33f406, league=Standard, id=e48eef62374f21e1f1feea5436893b45ec2690aea37534546e7a28b2782284a4, name=, typeLine=Titan's Geode Map of Power, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[3, 0]]), PropertyDto(name=Item Quantity, values=[[+24%, 1]]), PropertyDto(name=Item Rarity, values=[[+12%, 1]])], sockets=null, explicitMods=[Monsters gain 1 Power Charge every 20 seconds, Unique Boss has 25% increased Life, Unique Boss has 20% increased Area of Effect], enchantMods=null)
      wrapper.discard(DiscardBasis.PARSE_MAGIC_MAP);
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Rare) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Rare, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map67.png?scale=1&w=1&h=1&v=7806f58352e76963ac2bdaf1e515ecf8, league=Standard, id=4f4e21f2e662d2f7e82a1fa543ef7a7923501f17f6334ede7873aa7e6dde50c9, name=Tranquil Shadows, typeLine=Arena Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]]), PropertyDto(name=Item Quantity, values=[[+62%, 1]]), PropertyDto(name=Item Rarity, values=[[+25%, 1]]), PropertyDto(name=Monster Pack Size, values=[[+10%, 1]]), PropertyDto(name=Quality, values=[[+12%, 1]])], sockets=null, explicitMods=[Area has patches of shocking ground, Players have Elemental Equilibrium, Monsters reflect 13% of Elemental Damage, +40% Monster Fire Resistance], enchantMods=null)
      // todo: actually we can
      wrapper.discard(DiscardBasis.PARSE_RARE_MAP);
      return;
    }

    if (wrapper.getGroupDto() == GroupDto.map) {
      var tier = ItemUtility.extractMapTier(wrapper);
      item.setMapTier(tier);

      var series = ItemUtility.extractMapSeries(wrapper);
      item.setMapSeries(series);
    }

    if (wrapper.getGroupDto() != GroupDto.unique) {
      base.setFrameType(Rarity.Normal.ordinal());
    }
  }

  public void parseGem(Wrapper wrapper) {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    var level = ItemUtility.extractGemLevel(itemDto);
    if (level == null) {
      wrapper.discard(DiscardBasis.GEM_LEVEL_MISSING);
      return;
    }

    var quality = ItemUtility.extractGemQuality(itemDto);
    if (quality == null) {
      wrapper.discard(DiscardBasis.GEM_QUALITY_MISSING);
      return;
    }

    // Accept some quality ranges
    if (quality < 5) {
      quality = 0;
    } else if (quality > 17 && quality < 23) {
      quality = 20;
    } else if (quality != 23) {
      wrapper.discard(DiscardBasis.GEM_QUALITY_OUT_OF_RANGE);
      return;
    }

    // Begin the long block that filters out gems based on a number of properties
    if (ItemUtility.isSpecialSupportGem(itemDto)) {
      // Quality doesn't matter for lvl 3 and 4
      if (level > 2) {
        quality = 0;
      }
    } else if (itemDto.getTypeLine().equals("Brand Recall")) {
      if (level <= 2) {
        level = 1;
      } else if (level < 5) {
        wrapper.discard(DiscardBasis.GEM_BRAND_RECALL_LEVEL_OUT_OF_RANGE);
        return;
      }
    } else {
      // Accept some level ranges
      if (level < 5) {
        level = 1;
      } else if (level < 20) {
        wrapper.discard(DiscardBasis.GEM_LEVEL_OUT_OF_RANGE);
        return;
      }
    }

    if (itemDto.getIsCorrupted() != null && !itemDto.getIsCorrupted() && (level > 20 || quality > 20)) {
      wrapper.discard(DiscardBasis.GEM_API_BUG);
      return;
    }

    item.setGemLevel(level);
    item.setGemQuality(quality);
  }

  public void parseStackSize(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var item = wrapper.getItem();

    var stackSize = ItemUtility.extractMaxStackSize(itemDto);
    item.setStackSize(stackSize);
  }

  public void parseVariant(Wrapper wrapper) {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    var variant = itemVariantService.getVariation(itemDto);
    if (variant.isEmpty()) {
      return;
    }

    item.setVariation(variant.get().getVariation());
  }

  public void parseComplex(Wrapper wrapper) {
    var itemDto = wrapper.getItemDto();

    if (itemDto.getFrameType() == Rarity.Rare) {
      wrapper.discard(DiscardBasis.PARSE_COMPLEX_RARE);
      return;
    }

    if (itemDto.getFrameType() == Rarity.Magic) {
      wrapper.discard(DiscardBasis.PARSE_COMPLEX_MAGIC);
      return;
    }
  }

  public void parseCorrupted(Wrapper wrapper) {
    var categoryDto = wrapper.getCategoryDto();
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    if (categoryDto == CategoryDto.gem) {
      item.setCorrupted(itemDto.getIsCorrupted());
      return;
    }

    // todo: leaguestones, talismans, breach rings

    if (ItemUtility.isUnique(wrapper)) {
      if (corruptedItemService.isCorrupted(itemDto.getName())) {
        item.setCorrupted(itemDto.getIsCorrupted());
      }
    }
  }

  public void parseCategoryDto(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    if (itemDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_ITEM);
    }

    var apiCategory = itemDto.getExtended().getCategory();
    if (apiCategory == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_CATEGORY);
    }

    if (itemDto.getEnchantMods() != null) {
      wrapper.setCategoryDto(CategoryDto.enchantment);
      return;
    }

    // todo: abyssal jewels and belts and flasks are not included
    // todo: maps are included?
    if (ItemUtility.isCraftable(itemDto) && ItemUtility.hasInfluence(itemDto)) {
      wrapper.setCategoryDto(CategoryDto.base);
      return;
    }

    if (itemDto.getFrameType() == Rarity.Prophecy) {
      wrapper.setCategoryDto(CategoryDto.prophecy);
      return;
    }

    switch (apiCategory) {
      case "currency":
        wrapper.setCategoryDto(CategoryDto.currency);
        return;
      case "gems":
        wrapper.setCategoryDto(CategoryDto.gem);
        return;
      case "maps":
      case "watchstones":
        wrapper.setCategoryDto(CategoryDto.map);
        return;
      case "cards":
        wrapper.setCategoryDto(CategoryDto.card);
        return;
      case "flasks":
        wrapper.setCategoryDto(CategoryDto.flask);
        return;
      case "jewels":
        wrapper.setCategoryDto(CategoryDto.jewel);
        return;
      case "monsters":
        wrapper.setCategoryDto(CategoryDto.beast);
        return;
      case "armour":
        wrapper.setCategoryDto(CategoryDto.armour);
        return;
      case "accessories":
        wrapper.setCategoryDto(CategoryDto.accessory);
        return;
      case "weapons":
        wrapper.setCategoryDto(CategoryDto.weapon);
        return;
    }

    // todo: leaguestones have [apiCategory="leaguestones"]
    throw new ItemParseException(ParseExceptionBasis.PARSE_CATEGORY);
  }

  public void parseGroupDto(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var categoryDto = wrapper.getCategoryDto();

    if (itemDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_ITEM);
    } else if (categoryDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_CATEGORY);
    }

    var apiGroup = ItemUtility.getFirstApiGroup(itemDto);
    var iconCategory = ItemUtility.findIconCategory(itemDto);
    var acceptedGroups = groupMappingService.getGroups(categoryDto);

    switch (categoryDto) {
      case card:
        wrapper.setGroupDto(GroupDto.card);
        return;
      case flask:
        wrapper.setGroupDto(GroupDto.flask);
        return;
      case jewel:
        wrapper.setGroupDto(GroupDto.jewel);
        return;
      case prophecy:
        wrapper.setGroupDto(GroupDto.prophecy);
        return;
      case accessory:
      case weapon:
      case armour:
      case enchantment:
      case base:

        for (var group : acceptedGroups) {
          if (group.equals(apiGroup)) {
            wrapper.setGroupDto(GroupDto.valueOf(apiGroup));
            return;
          }
        }

        break;
      case currency:

        switch (iconCategory) {
          case "currency":
          case "divination": // stacked deck
            wrapper.setGroupDto(GroupDto.currency);
            return;
          case "essence":
            wrapper.setGroupDto(GroupDto.essence);
            return;
          case "breach":
            wrapper.setGroupDto(GroupDto.splinter);
            return;
          case "oils":
            wrapper.setGroupDto(GroupDto.oil);
            return;
          case "catalysts":
            wrapper.setGroupDto(GroupDto.catalyst);
            return;
          case "influence exalts":
            wrapper.setGroupDto(GroupDto.influence);
            return;
        }

        if (apiGroup != null) {
          switch (apiGroup) {
            case "piece":
              wrapper.setGroupDto(GroupDto.piece);
              return;
            case "resonator":
              wrapper.setGroupDto(GroupDto.resonator);
              return;
            case "fossil":
              wrapper.setGroupDto(GroupDto.fossil);
              return;
            case "incubator":
              wrapper.setGroupDto(GroupDto.incubator);
              return;
          }
        }

        if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Vial of ")) {
          wrapper.setGroupDto(GroupDto.vial);
          return;
        }

        if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Timeless")
          && itemDto.getTypeLine().endsWith("Splinter")) {
          wrapper.setGroupDto(GroupDto.splinter);
          return;
        }

        break;
      case gem:

        if ("vaalgems".equals(iconCategory)) {
          wrapper.setGroupDto(GroupDto.vaal);
          return;
        } else if ("activegem".equals(apiGroup)) {
          wrapper.setGroupDto(GroupDto.skill);
          return;
        } else if ("supportgem".equals(apiGroup)) {
          wrapper.setGroupDto(GroupDto.support);
          return;
        }

        break;
      case map:

        if (itemDto.getFrameType() == Rarity.Unique || itemDto.getFrameType() == Rarity.Relic) {
          wrapper.setGroupDto(GroupDto.unique);
          return;
        } else if ("breach".equals(iconCategory)) {
          wrapper.setGroupDto(GroupDto.fragment);
          return;
        } else if ("scarabs".equals(iconCategory)) {
          wrapper.setGroupDto(GroupDto.scarab);
          return;
        } else if (itemDto.getProperties() == null) {
          // mortal fragments
          wrapper.setGroupDto(GroupDto.fragment);
          return;
        } else if ("watchstones".equals(itemDto.getExtended().getCategory())) {
          wrapper.setGroupDto(GroupDto.watchstone);
          return;
        } else if (apiGroup == null) {
          wrapper.setGroupDto(GroupDto.map);
          return;
        }

        break;
      case beast:

        if ("sample".equals(apiGroup)) {
          wrapper.setGroupDto(GroupDto.sample);
          return;
        } else { // todo: find bestiary beast api group
          wrapper.setGroupDto(GroupDto.beast);
          return;
        }

    }

    throw new ItemParseException(ParseExceptionBasis.PARSE_GROUP);
  }

  public void parseItemBase(Wrapper wrapper) throws ItemParseException {
    var categoryDto = wrapper.getCategoryDto();
    var groupDto = wrapper.getGroupDto();
    var itemDto = wrapper.getItemDto();

    var category = Category.builder()
      .name(categoryDto.name())
      .build();
    var group = Group.builder()
      .name(groupDto.name())
      .build();

    if (itemDto.getFrameType() == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_FRAME_TYPE);
    }

    var builder = ItemBase.builder()
      .category(category)
      .group(group)
      .frameType(itemDto.getFrameType().ordinal());

    var name = itemDto.getName();
    if (name != null) {
      if (name.contains(">")) {
        name = name.substring(name.lastIndexOf(">") + 1);
      }

      // "Superior Ashen Wood Map" -> "Ashen Wood Map"
      if (name.startsWith("Superior ")) {
        name = name.replace("Superior ", "");
      }

      if (itemDto.getFrameType() == Rarity.Rare || StringUtils.isBlank(itemDto.getName())) {
        name = null;
      }
    }

    var baseType = itemDto.getTypeLine();
    if (baseType != null) {
      if (baseType.startsWith("Synthesised ")) {
        baseType = baseType.replace("Synthesised ", "");
      }
    }

    var itemBase = builder.name(name).baseType(baseType).build();
    wrapper.getItem().setBase(itemBase);
  }

}
