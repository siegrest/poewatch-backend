package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.ParseExceptionBasis;
import watch.poe.app.domain.wrapper.CategoryWrapper;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemUtility;

import java.util.Optional;

@Slf4j
@Service
public class ItemGroupingService {

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
      .apiGroup(CategorizationUtility.getFirstApiGroup(itemDto))
      .iconCategory(CategorizationUtility.findIconCategory(itemDto))
      .build();

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
        return parseFragmentGroups(categoryWrapper);
      case altart:
        return parseAltArtGroups(categoryWrapper);
      case base:
        return parseCraftingBaseGroups(categoryWrapper);
    }

    var oGroup = parseCommonGroups(categoryWrapper);
    if (oGroup.isPresent()) {
      return oGroup;
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
        log.info("[A18] (invalid splinter) {}", wrapper.getItemDto());
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
        return Optional.of(GroupDto.vial_currency);
      }
    }

    if (CategorizationUtility.isVialCurrency(wrapper.getItemDto())) {
      return Optional.of(GroupDto.vial_currency);
    }

    if (CategorizationUtility.isNetCurrency(itemDto)) {
      return Optional.of(GroupDto.net);
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

  public Optional<GroupDto> parseMapGroups(CategoryWrapper wrapper) throws ItemParseException {
    if (ItemUtility.isUnique(wrapper.getItemDto())) {
      return Optional.of(GroupDto.unique_map);
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Blighted")) {
      return Optional.of(GroupDto.blighted_map);
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Shaped ")) {
      return Optional.of(GroupDto.shaped_map);
    }

    if (wrapper.getItemDto().getExtended().getSubcategories() == null) {
      return Optional.of(GroupDto.regular_map);
    }

    // todo: remove me
    throw new ItemParseException(ParseExceptionBasis.DEV);
//    return Optional.empty();
  }

  public Optional<GroupDto> parseBeastGroups(CategoryWrapper wrapper) {
    if ("sample".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.sample);
    } else if ("beast".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.beast);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseCraftingBaseGroups(CategoryWrapper wrapper) {
    if (ItemUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return Optional.of(GroupDto.abyssal_jewel);
    }

    return parseCommonGroups(wrapper);
  }

  public Optional<GroupDto> parseFragmentGroups(CategoryWrapper wrapper) throws ItemParseException {
    if (CategorizationUtility.isWatchstone(wrapper.getItemDto())) {
      return Optional.of(GroupDto.watchstone);
    }

    if (CategorizationUtility.isBreachSplinter(wrapper.getItemDto())) {
      return Optional.of(GroupDto.breach_splinter);
    }

    if (CategorizationUtility.isScarab(wrapper.getItemDto())) {
      return Optional.of(GroupDto.scarab);
    }

    if (CategorizationUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return Optional.of(GroupDto.timeless_splinter);
    }

    if (CategorizationUtility.isTimelessEmblem(wrapper.getItemDto())) {
      return Optional.of(GroupDto.timeless_emblem);
    }

    if (CategorizationUtility.isSacrificeFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.sac_frag);
    }

    if (CategorizationUtility.isMortalFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.mortal_frag);
    }

    if (CategorizationUtility.isShaperGuardianFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.shaper_guardian_frag);
    }

    if (CategorizationUtility.isBreachstone(wrapper.getItemDto())) {
      return Optional.of(GroupDto.breachstone);
    }

    if (CategorizationUtility.isDivineVessel(wrapper.getItemDto())) {
      return Optional.of(GroupDto.misc_frag);
    }

    if (CategorizationUtility.isOfferingGoddess(wrapper.getItemDto())) {
      return Optional.of(GroupDto.misc_frag);
    }

    if (CategorizationUtility.isPaleCourtFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.pale_court_frag);
    }

    if (CategorizationUtility.isUberElderFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.uber_elder_frag);
    }

    if (CategorizationUtility.isElderGuardianFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.elder_guardian_frag);
    }

    // todo: remove me
    throw new ItemParseException(ParseExceptionBasis.DEV);
  }

}
