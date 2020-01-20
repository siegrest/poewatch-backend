package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.ParseExceptionBasis;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.utility.ItemUtility;

import java.util.Optional;

@Slf4j
@Service
public class ItemCategorizationService {

  public Optional<CategoryDto> parseCategoryDto(ItemWrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    if (itemDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_ITEM);
    }

    var apiCategory = itemDto.getExtended().getCategory();
    if (apiCategory == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_CATEGORY);
    }

    if (itemDto.getRaceReward() != null) {
      log.info("[A5] (altart) {}", wrapper);
      return Optional.of(CategoryDto.altart);
    }

    // todo: accessories and armours can have enchanted mods without being enchanted.
    if (ItemUtility.isLabEnchantment(wrapper)) {
      return Optional.of(CategoryDto.enchantment);
    }

    // todo: abyssal jewels and belts and flasks are not included
    // todo: maps are included?
    if (ItemUtility.isCraftable(itemDto) && ItemUtility.hasInfluence(itemDto)) {
      return Optional.of(CategoryDto.base);
    }

    if (itemDto.getFrameType() == Rarity.Prophecy) {
      return Optional.of(CategoryDto.prophecy);
    }

    switch (apiCategory) {
      case "currency":
        return Optional.of(CategoryDto.currency);
      case "gems":
        return Optional.of(CategoryDto.gem);
      case "maps":
      case "watchstones":
        return Optional.of(CategoryDto.map);
      case "cards":
        return Optional.of(CategoryDto.card);
      case "flasks":
        return Optional.of(CategoryDto.flask);
      case "jewels":
        return Optional.of(CategoryDto.jewel);
      case "monsters":
        return Optional.of(CategoryDto.beast);
      case "armour":
        return Optional.of(CategoryDto.armour);
      case "accessories":
        return Optional.of(CategoryDto.accessory);
      case "weapons":
        return Optional.of(CategoryDto.weapon);
      case "leaguestones":
        return Optional.of(CategoryDto.leaguestone);
    }

    // todo: leaguestones have [apiCategory="leaguestones"]
    return Optional.empty();
  }

  public Optional<GroupDto> parseGroupDto(ItemWrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var categoryDto = wrapper.getCategoryDto();

    if (itemDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_ITEM);
    } else if (categoryDto == null) {
      throw new ItemParseException(ParseExceptionBasis.MISSING_CATEGORY);
    }

    var categoryWrapper = CategoryWrapper.builder()
      .itemDto(itemDto)
      .categoryDto(categoryDto)
      .apiCategory(itemDto.getExtended().getCategory())
      .apiGroup(ItemUtility.getFirstApiGroup(itemDto))
      .iconCategory(ItemUtility.findIconCategory(itemDto))
      .build();

    var oGroup = parseCommonGroups(categoryWrapper);
    if (oGroup.isPresent()) {
      return oGroup;
    }

    switch (categoryDto) {
      case card:
        return Optional.of(GroupDto.card);
      case currency:
        return parseCurrencyGroups(categoryWrapper);
      case flask:
        return Optional.of(GroupDto.flask);
      case gem:
        return parseGemGroups(categoryWrapper);
      case jewel:
        return Optional.of(GroupDto.jewel);
      case map:
        return parseMapGroups(categoryWrapper);
      case prophecy:
        return Optional.of(GroupDto.prophecy);
      case beast:
        return parseBeastGroups(categoryWrapper);
      case leaguestone:
        return Optional.of(GroupDto.leaguestone);
      case fragment:
        break;
      case altart:
        return parseAltArtGroups(categoryWrapper);
      case accessory:
      case weapon:
      case armour:
      case enchantment:
      case base:
        return parseCommonGroups(categoryWrapper);
    }

    throw new ItemParseException(ParseExceptionBasis.UNHANDLED_CATEGORY);
  }

  public Optional<GroupDto> parseCommonGroups(CategoryWrapper wrapper) {
    var apiGroup = wrapper.getApiGroup();
    for (var group : GroupDto.values()) {
      if (group.name().equals(apiGroup)) {
        return Optional.of(GroupDto.valueOf(apiGroup));
      }
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseCurrencyGroups(CategoryWrapper wrapper) {
    switch (wrapper.getIconCategory()) {
      case "currency":
      case "divination": // stacked deck
        return Optional.of(GroupDto.currency);
      case "essence":
        return Optional.of(GroupDto.essence);
      case "breach":
        return Optional.of(GroupDto.splinter);
      case "oils":
        return Optional.of(GroupDto.oil);
      case "catalysts":
        return Optional.of(GroupDto.catalyst);
      case "influence exalts":
        return Optional.of(GroupDto.influence);
    }

    if (wrapper.getApiGroup() != null) {
      switch (wrapper.getApiGroup()) {
        case "piece":
          return Optional.of(GroupDto.piece);
        case "resonator":
          return Optional.of(GroupDto.resonator);
        case "fossil":
          return Optional.of(GroupDto.fossil);
        case "incubator":
          return Optional.of(GroupDto.incubator);
      }
    }

    var itemDto = wrapper.getItemDto();

    if (itemDto.getTypeLine() != null) {
      if (itemDto.getTypeLine().startsWith("Vial of ")) {
        return Optional.of(GroupDto.vial);
      }

      if (itemDto.getTypeLine().startsWith("Timeless") && itemDto.getTypeLine().endsWith("Splinter")) {
        return Optional.of(GroupDto.splinter);
      }
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseGemGroups(CategoryWrapper wrapper) {
    if ("vaalgems".equals(wrapper.getIconCategory())) {
      return Optional.of(GroupDto.vaal);
    } else if ("activegem".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.skill);
    } else if ("supportgem".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.support);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseAltArtGroups(CategoryWrapper wrapper) {
    var apiGroup = wrapper.getApiGroup();
    for (var group : GroupDto.values()) {
      if (group.name().equals(apiGroup)) {
        return Optional.of(GroupDto.valueOf(apiGroup));
      }
    }

    if (wrapper.getCategoryDto() == CategoryDto.altart && "jewel".equals(wrapper.getApiCategory())) {
      return Optional.of(GroupDto.jewel);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseMapGroups(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getFrameType() == Rarity.Unique || wrapper.getItemDto().getFrameType() == Rarity.Relic) {
      return Optional.of(GroupDto.unique);
    } else if ("breach".equals(wrapper.getIconCategory())) {
      return Optional.of(GroupDto.fragment);
    } else if ("scarabs".equals(wrapper.getIconCategory())) {
      return Optional.of(GroupDto.scarab);
    } else if (wrapper.getItemDto().getProperties() == null) {
      // mortal fragments
      return Optional.of(GroupDto.fragment);
    } else if ("watchstones".equals(wrapper.getItemDto().getExtended().getCategory())) {
      return Optional.of(GroupDto.watchstone);
    } else if (wrapper.getApiGroup() == null) {
      return Optional.of(GroupDto.map);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseBeastGroups(CategoryWrapper wrapper) {
    if ("sample".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.sample);
    } else if ("beast".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.beast);
    }

    return Optional.empty();
  }

}
