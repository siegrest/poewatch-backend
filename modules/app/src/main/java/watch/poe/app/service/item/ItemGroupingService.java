package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.GroupingExceptionBasis;
import watch.poe.app.domain.wrapper.CategoryWrapper;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemTypeUtility;
import watch.poe.app.utility.ItemUtility;

import java.util.Optional;

@Slf4j
@Service
public class ItemGroupingService {

  public GroupDto parseGroupDto(ItemWrapper itemWrapper) throws GroupingException {
    var itemDto = itemWrapper.getItemDto();
    var categoryDto = itemWrapper.getCategoryDto();
    var wrapper = CategoryWrapper.builder()
      .itemDto(itemDto)
      .categoryDto(categoryDto)
      .apiCategory(itemDto.getExtended().getCategory())
      .apiGroup(CategorizationUtility.getFirstApiGroup(itemDto))
      .iconCategory(CategorizationUtility.findIconCategory(itemDto))
      .build();

    switch (categoryDto) {
      case CARD:
        return GroupDto.CARD;
      case FLASK:
        return GroupDto.FLASK;
      case JEWEL:
        return GroupDto.JEWEL;
      case PROPHECY:
        return GroupDto.PROPHECY;
      case LEAGUESTONE:
        return GroupDto.LEAGUESTONE;
      case CURRENCY:
        return parseCurrencyGroups(wrapper);
      case GEM:
        return parseGemGroups(wrapper);
      case MAP:
        return parseMapGroups(wrapper);
      case BEAST:
        return parseBeastGroups(wrapper);
      case FRAGMENT:
        return parseFragmentGroups(wrapper);
      case ALTART:
        return parseAltArtGroups(wrapper);
      case CRAFTING_BASE:
        return parseCraftingBaseGroups(wrapper);
      case ACCESSORY:
      case ARMOUR:
      case WEAPON:
      case ENCHANTMENT:
        return parseCommonGroups(wrapper).orElseThrow(() -> new GroupingException(GroupingExceptionBasis.PARSE));
      default:
        throw new GroupingException(GroupingExceptionBasis.UNHANDLED_CATEGORY);
    }
  }

  private Optional<GroupDto> parseCommonGroups(CategoryWrapper wrapper) {
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
        return Optional.of(GroupDto.FISHING_ROD);
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

  private GroupDto parseCurrencyGroups(CategoryWrapper wrapper) throws GroupingException {
    switch (wrapper.getIconCategory()) {
      case "currency":
      case "divination": // stacked deck
        return GroupDto.CURRENCY;
      case "essence":
        return GroupDto.ESSENCE;
      case "oils":
        return GroupDto.OIL;
      case "catalysts":
        return GroupDto.CATALYST;
      case "influence exalts":
        return GroupDto.INFLUENCE_EXALT;
      case "breach":
        throw new GroupingException(GroupingExceptionBasis.DEPRECATED);
    }

    if (wrapper.getApiGroup() != null) {
      switch (wrapper.getApiGroup()) {
        case "piece":
          return GroupDto.HARBINGER_PIECE;
        case "resonator":
          return GroupDto.RESONATOR;
        case "fossil":
          return GroupDto.FOSSIL;
        case "incubator":
          return GroupDto.INCUBATOR;
      }
    }

    if (ItemTypeUtility.isVialCurrency(wrapper.getItemDto())) {
      return GroupDto.VIAL;
    }

    if (ItemTypeUtility.isNetCurrency(wrapper.getItemDto())) {
      return GroupDto.NET;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseGemGroups(CategoryWrapper wrapper) throws GroupingException {
    if ("vaalgems".equals(wrapper.getIconCategory())) {
      return GroupDto.VAAL_GEM;
    } else if ("activegem".equals(wrapper.getApiGroup())) {
      return GroupDto.SKILL_GEM;
    } else if ("supportgem".equals(wrapper.getApiGroup())) {
      return GroupDto.SUPPORT_GEM;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseAltArtGroups(CategoryWrapper wrapper) throws GroupingException {
    var oGrp = parseCommonGroups(wrapper);
    if (oGrp.isPresent()) {
      return oGrp.get();
    }

    if ("jewel".equals(wrapper.getApiCategory())) {
      return GroupDto.JEWEL;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseMapGroups(CategoryWrapper wrapper) throws GroupingException {
    if (ItemUtility.isUnique(wrapper.getItemDto())) {
      return GroupDto.UNIQUE_MAP;
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Blighted")) {
      return GroupDto.BLIGHTED_MAP;
    }

    if (wrapper.getItemDto().getTypeLine().startsWith("Shaped ")) {
      return GroupDto.SHAPED_MAP;
    }

    if (wrapper.getItemDto().getExtended().getSubcategories() == null) {
      return GroupDto.REGULAR_MAP;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseBeastGroups(CategoryWrapper wrapper) throws GroupingException {
    if ("sample".equals(wrapper.getApiGroup())) {
      return GroupDto.SAMPLE;
    } else if ("beast".equals(wrapper.getApiGroup())) {
      return GroupDto.BEAST;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseCraftingBaseGroups(CategoryWrapper wrapper) throws GroupingException {
    var oGrp = parseCommonGroups(wrapper);
    if (oGrp.isPresent()) {
      return oGrp.get();
    }

    if (ItemTypeUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return GroupDto.ABYSSAL_JEWEL;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseFragmentGroups(CategoryWrapper wrapper) throws GroupingException {
    if (ItemTypeUtility.isWatchstone(wrapper.getItemDto())) {
      return GroupDto.WATCHSTONE;
    }

    if (ItemTypeUtility.isBreachSplinter(wrapper.getItemDto())) {
      return GroupDto.BREACH_SPLINTER;
    }

    if (ItemTypeUtility.isScarab(wrapper.getItemDto())) {
      return GroupDto.SCARAB;
    }

    if (ItemTypeUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return GroupDto.TIMELESS_SPLINTER;
    }

    if (ItemTypeUtility.isTimelessEmblem(wrapper.getItemDto())) {
      return GroupDto.TIMELESS_EMBLEM;
    }

    if (ItemTypeUtility.isSacrificeFrag(wrapper.getItemDto())) {
      return GroupDto.SAC_FRAG;
    }

    if (ItemTypeUtility.isMortalFrag(wrapper.getItemDto())) {
      return GroupDto.MORTAL_FRAG;
    }

    if (ItemTypeUtility.isShaperGuardianFrag(wrapper.getItemDto())) {
      return GroupDto.SHAPER_GUARDIAN_FRAG;
    }

    if (ItemTypeUtility.isBreachstone(wrapper.getItemDto())) {
      return GroupDto.BREACHSTONE;
    }

    if (ItemTypeUtility.isDivineVessel(wrapper.getItemDto())) {
      return GroupDto.MISC_FRAG;
    }

    if (ItemTypeUtility.isOfferingGoddess(wrapper.getItemDto())) {
      return GroupDto.MISC_FRAG;
    }

    if (ItemTypeUtility.isPaleCourtFrag(wrapper.getItemDto())) {
      return GroupDto.PALE_COURT_FRAG;
    }

    if (ItemTypeUtility.isUberElderFrag(wrapper.getItemDto())) {
      return GroupDto.UBER_ELDER_FRAG;
    }

    if (ItemTypeUtility.isElderGuardianFrag(wrapper.getItemDto())) {
      return GroupDto.ELDER_GUARDIAN_FRAG;
    }

    if (ItemTypeUtility.isBreachBlessing(wrapper.getItemDto())) {
      return GroupDto.BREACH_BLESSING;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

}
