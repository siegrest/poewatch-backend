package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.utility.ItemUtility;

import java.util.Optional;

@Slf4j
@Service
public class ItemCategorizationService {

  public Optional<CategoryDto> parseCategoryDto(ItemWrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();
    var categoryWrapper = CategoryWrapper.builder()
      .itemDto(itemDto)
      .apiCategory(itemDto.getExtended().getCategory())
      .apiGroup(ItemUtility.getFirstApiGroup(itemDto))
      .iconCategory(ItemUtility.findIconCategory(itemDto))
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

    switch (categoryWrapper.getApiCategory()) {
      case "gems":
        return Optional.of(CategoryDto.gem);
      case "maps":
        return Optional.of(CategoryDto.map);
      case "watchstones":
        return Optional.of(CategoryDto.fragment);
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

  public Optional<CategoryDto> parseAltArtCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getRaceReward() != null) {
      return Optional.of(CategoryDto.altart);
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
    return Optional.of(CategoryDto.enchantment);
  }

  public Optional<CategoryDto> parseCraftingBaseCategory(CategoryWrapper wrapper) {
    // todo: abyssal jewels and belts and flasks are not included
    // todo: maps are included?

    if (wrapper.getItemDto().getIsCorrupted() != null && wrapper.getItemDto().getIsCorrupted()) {
      return Optional.empty();
    }

    var frameType = wrapper.getItemDto().getFrameType();
    if (frameType != Rarity.Normal && frameType != Rarity.Magic && frameType != Rarity.Rare) {
      return Optional.empty();
    }

    if (!ItemUtility.hasInfluence(wrapper.getItemDto())) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.base);
  }

  public Optional<CategoryDto> parseProphecyCategory(CategoryWrapper wrapper) {
    if (wrapper.getItemDto().getFrameType() == Rarity.Prophecy) {
      return Optional.of(CategoryDto.prophecy);
    }

    return Optional.empty();
  }

  public Optional<CategoryDto> parseCurrencyCategory(CategoryWrapper wrapper) {
    var itemDto = wrapper.getItemDto();

    if (itemDto.getTypeLine() != null) {
      // todo: redo based on api group
      if (itemDto.getTypeLine().contains("Splinter of ")) {
        log.info("[A7] (splinter) {}", itemDto);
        return Optional.empty();
      }

      // todo: redo based on api group
      if (itemDto.getTypeLine().startsWith("Timeless ") && itemDto.getTypeLine().endsWith(" Splinter")) {
        log.info("[A8] (timeless) {}", itemDto);
        return Optional.empty();
      }
    }

    return Optional.of(CategoryDto.currency);
  }

  public Optional<CategoryDto> parseFragmentCategory(CategoryWrapper wrapper) {
    var itemDto = wrapper.getItemDto();

    if ("watchstones".equals(wrapper.getApiCategory())) {
      return Optional.of(CategoryDto.fragment);
    }

    if ("breach".equals(wrapper.getIconCategory())) {
      log.info("[A9] (breach frag) {}", itemDto);
      return Optional.of(CategoryDto.fragment);
    }

    if ("scarabs".equals(wrapper.getIconCategory())) {
      log.info("[A10] (scarab) {}", itemDto);
      return Optional.of(CategoryDto.fragment);
    }

    // mortal fragments
    if (wrapper.getItemDto().getProperties() == null) {
      log.info("[A11] (fragment) {}", itemDto);
      return Optional.of(CategoryDto.fragment);
    }

    return Optional.empty();
  }

}
