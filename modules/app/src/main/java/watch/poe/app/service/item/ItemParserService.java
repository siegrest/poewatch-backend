package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.CategorizationService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemUtility;

@Service
@Slf4j
public final class ItemParserService {

  @Autowired
  private CategorizationService categorizationService;
  @Autowired
  private ItemVariantService itemVariantService;
  @Autowired
  private ItemBaseService itemBaseService;

  public void parse(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();

    var categoryDto = categorizationService.determineCategoryDto(itemDto);
    wrapper.setCategoryDto(categoryDto);

    var groupDto = categorizationService.determineGroupDto(itemDto, categoryDto);
    wrapper.setGroupDto(groupDto);

    var base = itemBaseService.getOrSave(wrapper);
    wrapper.setBase(base);

    parseIcon(wrapper);

    if (categoryDto == CategoryDto.map && (groupDto == GroupDto.map || groupDto == GroupDto.unique)) {
      parseMap(wrapper);
    }

    if (categoryDto == CategoryDto.gem) {
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

    if (ItemUtility.isComplex(itemDto, categoryDto)) {
      parseComplex(wrapper);
    }
  }

  public void parseIcon(Wrapper wrapper) throws ItemParseException {
    var icon = wrapper.getItemDto().getIcon();
    var newIcon = ItemUtility.formatIcon(icon);
    wrapper.getItem().setIcon(newIcon);
  }

  public void parseMap(Wrapper wrapper) {
    var base = wrapper.getBase();
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    if (wrapper.getGroupDto() == GroupDto.unique && !itemDto.isIdentified()) {
      // ItemDto(isIdentified=false, itemLevel=0, frameType=Unique, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/musicbox.png?scale=1&w=1&h=1&v=a8738647137a02c29c1b89d51d1bf58b, league=Standard, id=e703a5ae16defc94b606858fc4d53600be694ac8bed75f533b76f22ee90f03e3, name=, typeLine=Overgrown Shrine Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]])], sockets=null, explicitMods=null, enchantMods=null)
      // todo: actually we could
      wrapper.discard("Cannot parse unidentified unique map");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Magic) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Magic, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map66.png?scale=1&w=1&h=1&v=3557e95be294ee38dce858022f33f406, league=Standard, id=e48eef62374f21e1f1feea5436893b45ec2690aea37534546e7a28b2782284a4, name=, typeLine=Titan's Geode Map of Power, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[3, 0]]), PropertyDto(name=Item Quantity, values=[[+24%, 1]]), PropertyDto(name=Item Rarity, values=[[+12%, 1]])], sockets=null, explicitMods=[Monsters gain 1 Power Charge every 20 seconds, Unique Boss has 25% increased Life, Unique Boss has 20% increased Area of Effect], enchantMods=null)
      wrapper.discard("Cannot parse magic maps");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Rare) {
      // ItemDto(isIdentified=true, itemLevel=0, frameType=Rare, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map67.png?scale=1&w=1&h=1&v=7806f58352e76963ac2bdaf1e515ecf8, league=Standard, id=4f4e21f2e662d2f7e82a1fa543ef7a7923501f17f6334ede7873aa7e6dde50c9, name=Tranquil Shadows, typeLine=Arena Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]]), PropertyDto(name=Item Quantity, values=[[+62%, 1]]), PropertyDto(name=Item Rarity, values=[[+25%, 1]]), PropertyDto(name=Monster Pack Size, values=[[+10%, 1]]), PropertyDto(name=Quality, values=[[+12%, 1]])], sockets=null, explicitMods=[Area has patches of shocking ground, Players have Elemental Equilibrium, Monsters reflect 13% of Elemental Damage, +40% Monster Fire Resistance], enchantMods=null)
      // todo: actually we can
      wrapper.discard("Cannot parse rare maps");
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

    var level = ItemUtility.extractGemLevel(wrapper);
    var quality = ItemUtility.extractGemQuality(wrapper);

    if (wrapper.isDiscard()) {
      return;
    }

    // Accept some quality ranges
    if (quality < 5) {
      quality = 0;
    } else if (quality > 17 && quality < 23) {
      quality = 20;
    } else if (quality != 23) {
      wrapper.discard("Quality is out of range");
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
        wrapper.discard("Level is out of range for Brand Recall");
        return;
      }
    } else {
      // Accept some level ranges
      if (level < 5) {
        level = 1;
      } else if (level < 20) {
        wrapper.discard("Level is out of range for gem");
        return;
      }
    }

    if (itemDto.getIsCorrupted() != null && !itemDto.getIsCorrupted() && (level > 20 || quality > 20)) {
      wrapper.discard("Encountered API bug for gems");
      return;
    }

    item.setGemLevel(level);
    item.setGemQuality(quality);
    item.setGemCorrupted(itemDto.getIsCorrupted());
  }

  public void parseStackSize(Wrapper wrapper) {
    var item = wrapper.getItem();
    var stackSize = ItemUtility.extractMaxStackSize(wrapper);
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
      wrapper.discard("Cannot parse rare items");
      return;
    }

    if (itemDto.getFrameType() == Rarity.Magic) {
      wrapper.discard("Cannot parse magic items");
      return;
    }
  }

}
