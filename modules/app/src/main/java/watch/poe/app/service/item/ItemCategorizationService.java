package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.domain.wrapper.CategoryWrapper;
import watch.poe.app.domain.wrapper.ItemWrapper;
import watch.poe.app.utility.CategorizationUtility;
import watch.poe.app.utility.ItemUtility;

import java.util.Optional;

@Slf4j
@Service
public class ItemCategorizationService {

  public Optional<CategoryDto> parseCategoryDto(ItemWrapper wrapper) {
    var itemDto = wrapper.getItemDto();
    var categoryWrapper = CategoryWrapper.builder()
      .itemDto(itemDto)
      .apiCategory(itemDto.getExtended().getCategory())
      .apiGroup(CategorizationUtility.getFirstApiGroup(itemDto))
      .iconCategory(CategorizationUtility.findIconCategory(itemDto))
      .build();

    var oCat = parseAltArtCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseEnchantmentCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseCraftingBaseCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseProphecyCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseCurrencyCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseFragmentCategory(categoryWrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    return parseSpecificCategories(categoryWrapper);
  }

  public Optional<CategoryDto> parseAltArtCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getRaceReward() != null) {
      return Optional.of(CategoryDto.ALTART);
    }

    return Optional.empty();
  }

  public Optional<CategoryDto> parseEnchantmentCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getEnchantMods() == null) {
      return Optional.empty();
    }

    if (!"armour".equals(wrapper.getApiCategory())) {
      return Optional.empty();
    }

    if ("helmets".equals(wrapper.getApiGroup())
      || "gloves".equals(wrapper.getApiGroup())
      || "boots".equals(wrapper.getApiGroup())) {
      return Optional.empty();
    }

    // todo: accessories and armours can have enchanted mods without being enchanted.
    return Optional.of(CategoryDto.ENCHANTMENT);
  }

  public Optional<CategoryDto> parseCraftingBaseCategory(CategoryWrapper wrapper) {
    if (ItemUtility.isCorrupted(wrapper.getItemDto())) {
      return Optional.empty();
    }

    if (!ItemUtility.isCraftable(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // todo: abyssal belts and flasks are not included
    if (ItemUtility.isAbyssalJewel(wrapper.getItemDto())) {
      return Optional.of(CategoryDto.CRAFTING_BASE);
    }

    if (!ItemUtility.hasInfluence(wrapper.getItemDto())) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.CRAFTING_BASE);
  }

  public Optional<CategoryDto> parseProphecyCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getFrameType() == Rarity.Prophecy) {
      return Optional.of(CategoryDto.PROPHECY);
    }

    return Optional.empty();
  }

  public Optional<CategoryDto> parseCurrencyCategory(CategoryWrapper wrapper) {
    if (!"currency".equals(wrapper.getApiCategory())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (CategorizationUtility.isBreachSplinter(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (CategorizationUtility.isTimelessSplinter(wrapper.getItemDto())) {
      return Optional.empty();
    }

    // categorized as fragments
    if (CategorizationUtility.isBreachBlessing(wrapper.getItemDto())) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.CURRENCY);
  }

  public Optional<CategoryDto> parseFragmentCategory(CategoryWrapper wrapper) {
    if (!"maps".equals(wrapper.getApiCategory()) && !"currency".equals(wrapper.getApiCategory())) {
      return Optional.empty();
    }

    var itemDto = wrapper.getItemDto();

    if (CategorizationUtility.getApiGroups(itemDto).contains("fragment")) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isWatchstone(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isBreachSplinter(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isScarab(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isTimelessSplinter(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isBreachBlessing(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    if (CategorizationUtility.isReliquaryKey(itemDto)) {
      return Optional.of(CategoryDto.FRAGMENT);
    }

    return Optional.empty();
  }

  public Optional<CategoryDto> parseSpecificCategories(CategoryWrapper wrapper) {
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
