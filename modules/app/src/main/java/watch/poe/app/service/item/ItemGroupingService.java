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
      case CARD:
        return Optional.of(GroupDto.CARD);
      case CURRENCY:
        return parseCurrencyGroups(categoryWrapper);
      case FLASK:
        return Optional.of(GroupDto.FLASK);
      case GEM:
        return parseGemGroups(categoryWrapper);
      case JEWEL:
        return Optional.of(GroupDto.JEWEL);
      case MAP:
        return parseMapGroups(categoryWrapper);
      case PROPHECY:
        return Optional.of(GroupDto.PROPHECY);
      case BEAST:
        return parseBeastGroups(categoryWrapper);
      case LEAGUESTONE:
        return Optional.of(GroupDto.LEAGUESTONE);
      case FRAGMENT:
        return parseFragmentGroups(categoryWrapper);
      case ALTART:
        return parseAltArtGroups(categoryWrapper);
      case CRAFTING_BASE:
        return parseCraftingBaseGroups(categoryWrapper);
    }

    var oGroup = parseCommonGroups(categoryWrapper);
    if (oGroup.isPresent()) {
      return oGroup;
    }

    throw new ItemParseException(ParseExceptionBasis.UNHANDLED_CATEGORY);
  }

  public Optional<GroupDto> parseCommonGroups(CategoryWrapper wrapper) {
    if (wrapper.getApiGroup() == null) {
      return Optional.empty();
    }

    switch (wrapper.getApiGroup()) {
      case "amulet":
        return Optional.of(GroupDto.AMULET);
      case "belt":
        return Optional.of(GroupDto.BELT);
      case "ring":
        return Optional.of(GroupDto.RING);

      case "boots":
        return Optional.of(GroupDto.BOOTS);
      case "chest":
        return Optional.of(GroupDto.CHEST);
      case "gloves":
        return Optional.of(GroupDto.GLOVES);
      case "helmet":
        return Optional.of(GroupDto.HELMET);
      case "quiver":
        return Optional.of(GroupDto.QUIVER);
      case "shield":
        return Optional.of(GroupDto.SHIELD);

      case "bow":
        return Optional.of(GroupDto.BOW);
      case "claw":
        return Optional.of(GroupDto.CLAW);
      case "dagger":
        return Optional.of(GroupDto.DAGGER);
      case "oneaxe":
        return Optional.of(GroupDto.ONE_HAND_AXE);
      case "onemace":
        return Optional.of(GroupDto.ONE_HAND_MACE);
      case "onesword":
        return Optional.of(GroupDto.ONE_HAND_SWORD);
      case "rod":
        return Optional.of(GroupDto.ROD);
      case "sceptre":
        return Optional.of(GroupDto.SCEPTRE);
      case "staff":
        return Optional.of(GroupDto.STAFF);
      case "twoaxe":
        return Optional.of(GroupDto.TWO_HAND_AXE);
      case "twomace":
        return Optional.of(GroupDto.TWO_HAND_MACE);
      case "twosword":
        return Optional.of(GroupDto.TWO_HAND_SWORD);
      case "wand":
        return Optional.of(GroupDto.WAND);
      case "runedagger":
        return Optional.of(GroupDto.RUNE_DAGGER);
      case "warstaff":
        return Optional.of(GroupDto.WARSTAFF);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseCurrencyGroups(CategoryWrapper wrapper) throws ItemParseException {
    switch (wrapper.getIconCategory()) {
      case "currency":
      case "divination": // stacked deck
        return Optional.of(GroupDto.CURRENCY);
      case "essence":
        return Optional.of(GroupDto.ESSENCE);
      case "breach":
        throw new ItemParseException(ParseExceptionBasis.DEPRECATED_GROUP);
      case "oils":
        return Optional.of(GroupDto.OIL);
      case "catalysts":
        return Optional.of(GroupDto.CATALYST);
      case "influence exalts":
        return Optional.of(GroupDto.INFLUENCE_EXALT);
    }

    if (wrapper.getApiGroup() != null) {
      switch (wrapper.getApiGroup()) {
        case "piece":
          return Optional.of(GroupDto.HARBINGER_PIECE);
        case "resonator":
          return Optional.of(GroupDto.RESONATOR);
        case "fossil":
          return Optional.of(GroupDto.FOSSIL);
        case "incubator":
          return Optional.of(GroupDto.INCUBATOR);
      }
    }

    var itemDto = wrapper.getItemDto();

    if (itemDto.getTypeLine() != null) {
      if (itemDto.getTypeLine().startsWith("Vial of ")) {
        return Optional.of(GroupDto.VIAL);
      }
    }

    if (CategorizationUtility.isVialCurrency(wrapper.getItemDto())) {
      return Optional.of(GroupDto.VIAL);
    }

    if (CategorizationUtility.isNetCurrency(itemDto)) {
      return Optional.of(GroupDto.NET);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseGemGroups(CategoryWrapper wrapper) {
    if ("vaalgems".equals(wrapper.getIconCategory())) {
      return Optional.of(GroupDto.VAAL_GEM);
    } else if ("activegem".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.SKILL_GEM);
    } else if ("supportgem".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.SUPPORT_GEM);
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

    if (wrapper.getCategoryDto() == CategoryDto.ALTART && "jewel".equals(wrapper.getApiCategory())) {
      return Optional.of(GroupDto.JEWEL);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseMapGroups(CategoryWrapper wrapper) throws ItemParseException {
    if (ItemUtility.isUnique(wrapper.getItemDto())) {
      return Optional.of(GroupDto.UNIQUE_MAP);
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Blighted")) {
      return Optional.of(GroupDto.BLIGHTED_MAP);
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Shaped ")) {
      return Optional.of(GroupDto.SHAPED_MAP);
    }

    if (wrapper.getItemDto().getExtended().getSubcategories() == null) {
      return Optional.of(GroupDto.REGULAR_MAP);
    }

    // todo: remove me
    throw new ItemParseException(ParseExceptionBasis.DEV);
//    return Optional.empty();
  }

  public Optional<GroupDto> parseBeastGroups(CategoryWrapper wrapper) {
    if ("sample".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.SAMPLE);
    } else if ("beast".equals(wrapper.getApiGroup())) {
      return Optional.of(GroupDto.BEAST);
    }

    return Optional.empty();
  }

  public Optional<GroupDto> parseCraftingBaseGroups(CategoryWrapper wrapper) {
    if (ItemUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return Optional.of(GroupDto.ABYSSAL_JEWEL);
    }

    return parseCommonGroups(wrapper);
  }

  public Optional<GroupDto> parseFragmentGroups(CategoryWrapper wrapper) throws ItemParseException {
    if (CategorizationUtility.isWatchstone(wrapper.getItemDto())) {
      return Optional.of(GroupDto.WATCHSTONE);
    }

    if (CategorizationUtility.isBreachSplinter(wrapper.getItemDto())) {
      return Optional.of(GroupDto.BREACH_SPLINTER);
    }

    if (CategorizationUtility.isScarab(wrapper.getItemDto())) {
      return Optional.of(GroupDto.SCARAB);
    }

    if (CategorizationUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return Optional.of(GroupDto.TIMELESS_SPLINTER);
    }

    if (CategorizationUtility.isTimelessEmblem(wrapper.getItemDto())) {
      return Optional.of(GroupDto.TIMELESS_EMBLEM);
    }

    if (CategorizationUtility.isSacrificeFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.SAC_FRAG);
    }

    if (CategorizationUtility.isMortalFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.MORTAL_FRAG);
    }

    if (CategorizationUtility.isShaperGuardianFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.SHAPER_GUARDIAN_FRAG);
    }

    if (CategorizationUtility.isBreachstone(wrapper.getItemDto())) {
      return Optional.of(GroupDto.BREACHSTONE);
    }

    if (CategorizationUtility.isDivineVessel(wrapper.getItemDto())) {
      return Optional.of(GroupDto.MISC_FRAG);
    }

    if (CategorizationUtility.isOfferingGoddess(wrapper.getItemDto())) {
      return Optional.of(GroupDto.MISC_FRAG);
    }

    if (CategorizationUtility.isPaleCourtFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.PALE_COURT_FRAG);
    }

    if (CategorizationUtility.isUberElderFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.UBER_ELDER_FRAG);
    }

    if (CategorizationUtility.isElderGuardianFrag(wrapper.getItemDto())) {
      return Optional.of(GroupDto.ELDER_GUARDIAN_FRAG);
    }

    if (CategorizationUtility.isBreachBlessing(wrapper.getItemDto())) {
      return Optional.of(GroupDto.BREACH_BLESSING);
    }

    // todo: remove me
    throw new ItemParseException(ParseExceptionBasis.DEV);
  }

}
