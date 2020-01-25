package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.GroupingExceptionBasis;
import watch.poe.app.domain.wrapper.CategoryWrapper;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.exception.GroupingException;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemUtility;

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
        return parseCommonGroups(wrapper);
      default:
        throw new GroupingException(GroupingExceptionBasis.UNHANDLED_CATEGORY);
    }
  }

  private GroupDto parseCommonGroups(CategoryWrapper wrapper) throws GroupingException {
    if (wrapper.getApiGroup() == null) {
      throw new GroupingException(GroupingExceptionBasis.PARSE);
    }

    switch (wrapper.getApiGroup()) {
      case "amulet":
        return GroupDto.AMULET;
      case "belt":
        return GroupDto.BELT;
      case "ring":
        return GroupDto.RING;

      case "boots":
        return GroupDto.BOOTS;
      case "chest":
        return GroupDto.CHEST;
      case "gloves":
        return GroupDto.GLOVES;
      case "helmet":
        return GroupDto.HELMET;
      case "quiver":
        return GroupDto.QUIVER;
      case "shield":
        return GroupDto.SHIELD;

      case "bow":
        return GroupDto.BOW;
      case "claw":
        return GroupDto.CLAW;
      case "dagger":
        return GroupDto.DAGGER;
      case "oneaxe":
        return GroupDto.ONE_HAND_AXE;
      case "onemace":
        return GroupDto.ONE_HAND_MACE;
      case "onesword":
        return GroupDto.ONE_HAND_SWORD;
      case "rod":
        return GroupDto.ROD;
      case "sceptre":
        return GroupDto.SCEPTRE;
      case "staff":
        return GroupDto.STAFF;
      case "twoaxe":
        return GroupDto.TWO_HAND_AXE;
      case "twomace":
        return GroupDto.TWO_HAND_MACE;
      case "twosword":
        return GroupDto.TWO_HAND_SWORD;
      case "wand":
        return GroupDto.WAND;
      case "runedagger":
        return GroupDto.RUNE_DAGGER;
      case "warstaff":
        return GroupDto.WARSTAFF;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
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

    if (CategorizationUtility.isVialCurrency(wrapper.getItemDto())) {
      return GroupDto.VIAL;
    }

    if (CategorizationUtility.isNetCurrency(wrapper.getItemDto())) {
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
    var apiGroup = wrapper.getApiGroup();
    for (var group : GroupDto.values()) {
      if (group.name().equals(apiGroup)) {
        return GroupDto.valueOf(apiGroup);
      }
    }

    if (wrapper.getCategoryDto() == CategoryDto.ALTART && "jewel".equals(wrapper.getApiCategory())) {
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
    if (ItemUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return GroupDto.ABYSSAL_JEWEL;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

  private GroupDto parseFragmentGroups(CategoryWrapper wrapper) throws GroupingException {
    if (CategorizationUtility.isWatchstone(wrapper.getItemDto())) {
      return GroupDto.WATCHSTONE;
    }

    if (CategorizationUtility.isBreachSplinter(wrapper.getItemDto())) {
      return GroupDto.BREACH_SPLINTER;
    }

    if (CategorizationUtility.isScarab(wrapper.getItemDto())) {
      return GroupDto.SCARAB;
    }

    if (CategorizationUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return GroupDto.TIMELESS_SPLINTER;
    }

    if (CategorizationUtility.isTimelessEmblem(wrapper.getItemDto())) {
      return GroupDto.TIMELESS_EMBLEM;
    }

    if (CategorizationUtility.isSacrificeFrag(wrapper.getItemDto())) {
      return GroupDto.SAC_FRAG;
    }

    if (CategorizationUtility.isMortalFrag(wrapper.getItemDto())) {
      return GroupDto.MORTAL_FRAG;
    }

    if (CategorizationUtility.isShaperGuardianFrag(wrapper.getItemDto())) {
      return GroupDto.SHAPER_GUARDIAN_FRAG;
    }

    if (CategorizationUtility.isBreachstone(wrapper.getItemDto())) {
      return GroupDto.BREACHSTONE;
    }

    if (CategorizationUtility.isDivineVessel(wrapper.getItemDto())) {
      return GroupDto.MISC_FRAG;
    }

    if (CategorizationUtility.isOfferingGoddess(wrapper.getItemDto())) {
      return GroupDto.MISC_FRAG;
    }

    if (CategorizationUtility.isPaleCourtFrag(wrapper.getItemDto())) {
      return GroupDto.PALE_COURT_FRAG;
    }

    if (CategorizationUtility.isUberElderFrag(wrapper.getItemDto())) {
      return GroupDto.UBER_ELDER_FRAG;
    }

    if (CategorizationUtility.isElderGuardianFrag(wrapper.getItemDto())) {
      return GroupDto.ELDER_GUARDIAN_FRAG;
    }

    if (CategorizationUtility.isBreachBlessing(wrapper.getItemDto())) {
      return GroupDto.BREACH_BLESSING;
    }

    throw new GroupingException(GroupingExceptionBasis.PARSE);
  }

}
