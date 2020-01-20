package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
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

    var oCat = parseAltArtCategory(wrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseEnchantmentCategory(wrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseCraftingBaseCategory(wrapper);
    if (oCat.isPresent()) {
      return oCat;
    }

    oCat = parseProphecyCategory(wrapper);
    if (oCat.isPresent()) {
      return oCat;
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

  public Optional<CategoryDto> parseAltArtCategory(ItemWrapper wrapper) {
    if (wrapper.getItemDto().getRaceReward() != null) {
      return Optional.of(CategoryDto.altart);
    }

    return Optional.empty();
  }

  public Optional<CategoryDto> parseEnchantmentCategory(ItemWrapper wrapper) {
    var extended = wrapper.getItemDto().getExtended();
    if (extended == null) {
      return Optional.empty();
    }

    var apiCategory = extended.getCategory();
    var itemDto = wrapper.getItemDto();

    if (itemDto.getEnchantMods() == null) {
      return Optional.empty();
    }

    if (!"armour".equals(apiCategory)) {
      return Optional.empty();
    }

    var firstGroup = ItemUtility.getFirstApiGroup(itemDto);

    if ("helmets".equals(firstGroup) || "gloves".equals(firstGroup) || "boots".equals(firstGroup)) {
      return Optional.empty();
    }

    // todo: accessories and armours can have enchanted mods without being enchanted.
    return Optional.of(CategoryDto.enchantment);
  }

  public Optional<CategoryDto> parseCraftingBaseCategory(ItemWrapper wrapper) {
    // todo: abyssal jewels and belts and flasks are not included
    // todo: maps are included?
    var itemDto = wrapper.getItemDto();
    var frameType = itemDto.getFrameType();
    var corrupted = itemDto.getIsCorrupted() != null && itemDto.getIsCorrupted();

    if (corrupted) {
      return Optional.empty();
    }

    if (frameType != Rarity.Normal && frameType != Rarity.Magic && frameType != Rarity.Rare) {
      return Optional.empty();
    }

    if (!ItemUtility.hasInfluence(itemDto)) {
      return Optional.empty();
    }

    return Optional.of(CategoryDto.base);
  }

  public Optional<CategoryDto> parseProphecyCategory(ItemWrapper wrapper) {
    if (wrapper.getItemDto().getFrameType() == Rarity.Prophecy) {
      return Optional.of(CategoryDto.prophecy);
    }

    return Optional.empty();
  }

}
