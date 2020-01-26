package watch.poe.app.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.*;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.resource.UniqueMapIdentificationService;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.ItemBase;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ItemBaseParserService {

  private final UniqueMapIdentificationService uniqueMapService;

  public ItemBase parse(CategoryDto categoryDto, GroupDto groupDto, ItemDto itemDto) throws ItemParseException {
    checkFrameType(categoryDto, itemDto);

    var category = Category.builder().name(categoryDto.name()).build();
    var group = Group.builder().name(groupDto.name()).build();

    var name = parseName(categoryDto, groupDto, itemDto);
    var baseType = parseBaseType(categoryDto, groupDto, itemDto);

    var base = ItemBase.builder()
      .category(category)
      .group(group)
      .frameType(itemDto.getFrameType().ordinal())
      .name(name)
      .baseType(baseType)
      .build();

    if (ItemUtility.isLabEnchantment(itemDto)) {
      parseEnchantment(base, itemDto);
    }

    return base;
  }

  private String parseName(CategoryDto categoryDto, GroupDto groupDto, ItemDto itemDto) throws ItemParseException {
    if (!itemDto.isIdentified() && itemDto.getFrameType() == Rarity.Unique) {
      throw new ItemParseException(ParseExceptionBasis.PARSE_UNID_UNIQUE_ITEM);
    }

    if (itemDto.getFrameType() == Rarity.Rare || StringUtils.isBlank(itemDto.getName())) {
      return null;
    }

    var name = itemDto.getName();

    if (categoryDto == CategoryDto.MAP && groupDto == GroupDto.UNIQUE_MAP && !itemDto.isIdentified()) {
      // ItemDto(isIdentified=false, itemLevel=0, frameType=Unique, isCorrupted=null, isSynthesised=null, icon=http://web.poecdn.com/image/Art/2DItems/Maps/musicbox.png?scale=1&w=1&h=1&v=a8738647137a02c29c1b89d51d1bf58b, league=Standard, id=e703a5ae16defc94b606858fc4d53600be694ac8bed75f533b76f22ee90f03e3, name=, typeLine=Overgrown Shrine Map, note=null, stackSize=null, prophecyText=null, raceReward=null, influences=null, extended=ExtendedDto(category=maps, subcategories=null), properties=[PropertyDto(name=Map Tier, values=[[4, 0]])], sockets=null, explicitMods=null, enchantMods=null)

      var mapMatch = uniqueMapService.identifyMap(itemDto);
      if (mapMatch.isEmpty()) {
        throw new ItemParseException(ParseExceptionBasis.PARSE_UNID_UNIQUE_MAP);
      }

      name = mapMatch.get().getName();
    }

    return name;
  }

  private String parseBaseType(CategoryDto categoryDto, GroupDto groupDto, ItemDto itemDto) {
    var baseType = itemDto.getTypeLine();

    if (baseType == null || StringUtils.isBlank(baseType)) {
      return null;
    }

    if (categoryDto == CategoryDto.MAP && CategorizationUtility.hasQuality(itemDto)) {
      baseType = CategorizationUtility.replacePrefix("Superior ", baseType);
    }

    if (itemDto.getSynthesised() != null && itemDto.getSynthesised()) {
      baseType = CategorizationUtility.replacePrefix("Synthesised ", baseType);
    }

    return baseType;
  }

  private void checkFrameType(CategoryDto categoryDto, ItemDto itemDto) throws ItemParseException {
    if (itemDto.getFrameType() == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_FRAME_TYPE);
    }

    if (itemDto.getFrameType() == Rarity.Rare) {
      throw new ItemParseException(DiscardBasis.PARSE_COMPLEX_RARE);
    }

    if (itemDto.getFrameType() == Rarity.Magic) {
      throw new ItemParseException(DiscardBasis.PARSE_COMPLEX_MAGIC);
    }

    if (itemDto.getFrameType() != Rarity.Unique
      && (categoryDto == CategoryDto.ARMOUR
      || categoryDto == CategoryDto.WEAPON
      || categoryDto == CategoryDto.ACCESSORY
      || categoryDto == CategoryDto.FLASK
      || categoryDto == CategoryDto.JEWEL)) {
      throw new ItemParseException(DiscardBasis.UNIQUE_ONLY);
    }

  }

  private void parseEnchantment(ItemBase base, ItemDto itemDto) {
    var enchantName = ItemUtility.extractEnchantmentName(itemDto);
    // todo: or maybe the other way around?
    base.setName(enchantName);
    base.setBaseType(null);
    base.setFrameType(0);
  }

}
