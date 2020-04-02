package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.ParseExceptionBasis;
import watch.poe.app.dto.wrapper.CategoryWrapper;
import watch.poe.app.dto.wrapper.ItemWrapper;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemTypeUtility;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.domain.FrameType;

import java.util.Optional;

@Slf4j
@Service
public class ItemCategorizationService {

  public CategoryDto parseCategoryDto(ItemWrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var categoryWrapper = CategoryWrapper.builder()
      .itemDto(itemDto)
      .apiCategory(itemDto.getExtended().getCategory())
      .apiGroup(CategorizationUtility.getFirstApiGroup(itemDto))
      .iconCategory(CategorizationUtility.findIconCategory(itemDto))
      .build();

    var oCat = parseAltArtCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseEnchantmentCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseCraftingBaseCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseProphecyCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseCurrencyCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseFragmentCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    oCat = parseSpecificCategories(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat.get();
    }

    throw new ItemParseException(ParseExceptionBasis.PARSE_CATEGORY);
  }

  private Optional<CategoryDto> parseAltArtCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getRaceReward() != null) {
      return Optional.of(CategoryDto.ALTART);
    }

    return Optional.empty();
  }

  private Optional<CategoryDto> parseEnchantmentCategory(CategoryWrapper wrapper) {
    if (ItemTypeUtility.isLabEnchantment(wrapper.getItemDto())) {
      return Optional.of(CategoryDto.ENCHANTMENT);
    }

    // todo: accessories and armours can have enchanted mods without being enchanted.
    return Optional.empty();
  }

  private Optional<CategoryDto> parseCraftingBaseCategory(CategoryWrapper wrapper) {
    if (ItemUtility.isCorrupted(wrapper.getItemDto())) {
      return Optional.empty();
    }

    if (!ItemUtility.isCraftable(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // todo: abyssal belts and flasks are not included
    if (ItemTypeUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return Optional.of(CategoryDto.CRAFTING_BASE);
    }

    if (!ItemUtility.hasInfluence(wrapper.getItemDto())) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.CRAFTING_BASE);
  }

  private Optional<CategoryDto> parseProphecyCategory(CategoryWrapper wrapper) {
    if (FrameType.PROPHECY.is(wrapper.getItemDto().getFrameType())) {
      return Optional.of(CategoryDto.PROPHECY);
    }

    return Optional.empty();
  }

  private Optional<CategoryDto> parseCurrencyCategory(CategoryWrapper wrapper) {
    if (!"currency".equals(wrapper.getApiCategory())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (ItemTypeUtility.isBreachSplinter(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (ItemTypeUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (ItemTypeUtility.isBreachBlessing(wrapper.getItemDto())) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.CURRENCY);
  }

  private Optional<CategoryDto> parseFragmentCategory(CategoryWrapper wrapper) {
    if (!"maps".equals(wrapper.getApiCategory()) && !"currency".equals(wrapper.getApiCategory())) {
      return Optional.empty();
    }

    var itemDto = wrapper.getItemDto();

    if (CategorizationUtility.getApiGroups(itemDto).contains("fragment")) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isWatchstone(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isBreachSplinter(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isScarab(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isTimelessSplinter(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isBreachBlessing(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (ItemTypeUtility.isReliquaryKey(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    return Optional.empty();
  }

  private Optional<CategoryDto> parseSpecificCategories(CategoryWrapper wrapper) {
    switch (wrapper.getApiCategory()) {
      case "gems":
        return Optional.of(CategoryDto.GEM);
      case "maps":
        return Optional.of(CategoryDto.MAP);
      case "watchstones":
        return Optional.of(CategoryDto.FRAGMENT);
      case "cards":
        return Optional.of(CategoryDto.CARD);
      case "flasks":
        return Optional.of(CategoryDto.FLASK);
      case "jewels":
        return Optional.of(CategoryDto.JEWEL);
      case "monsters":
        return Optional.of(CategoryDto.BEAST);
      case "armour":
        return Optional.of(CategoryDto.ARMOUR);
      case "accessories":
        return Optional.of(CategoryDto.ACCESSORY);
      case "weapons":
        return Optional.of(CategoryDto.WEAPON);
      case "leaguestones":
        return Optional.of(CategoryDto.LEAGUESTONE);
    }

    return Optional.empty();
  }

}
