package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.CategorizationService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.ItemBase;

import java.util.Set;

@Component
@Slf4j
public final class ItemParserService {

  @Autowired
  private CategorizationService categorizationService;
  @Autowired
  private ItemVariantService itemVariantService;

  public void parse(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();

    var categoryDto = categorizationService.determineCategoryDto(itemDto);
    wrapper.setCategoryDto(categoryDto);

    var groupDto = categorizationService.determineGroupDto(itemDto, categoryDto);
    wrapper.setGroupDto(groupDto);

    var base = parseBase(wrapper);
    wrapper.setBase(base);

    parseIcon(wrapper);

    if (categoryDto == CategoryDto.map && (groupDto == GroupDto.map || groupDto == GroupDto.unique)) {
      parseMap(wrapper);
    }

    if (categoryDto == CategoryDto.gem) {
      parseGem(wrapper);
    }

    if (ItemUtility.isStackable(itemDto)) {
      parseStackSize(wrapper);
    }

    if (ItemUtility.isLinkable(wrapper)) {
      var links = ItemUtility.extractLinks(wrapper);
      wrapper.getItem().setLinks(links);
    }

    if (itemVariantService.hasVariation(wrapper.getItemDto())) {
      parseVariant(wrapper);
    }
  }

  public ItemBase parseBase(Wrapper wrapper) throws ItemParseException {
    var categoryDto = wrapper.getCategoryDto();
    var groupDto = wrapper.getGroupDto();
    var itemDto = wrapper.getItemDto();

    var category = categorizationService.categoryDtoToCategory(categoryDto);
    var group = categorizationService.groupDtoToGroup(groupDto);

    if (itemDto.getFrameType() == null) {
      throw new ItemParseException("Invalid frame type");
    }

    var builder = ItemBase.builder()
      .category(category)
      .group(group)
      .frameType(itemDto.getFrameType().ordinal())
      .items(Set.of());

    var name = itemDto.getName();
    if (name != null) {
      if (name.contains(">")) {
        name = name.substring(name.lastIndexOf(">") + 1);
      }

      // "Superior Ashen Wood Map" -> "Ashen Wood Map"
      if (name.startsWith("Superior ")) {
        name = name.replace("Superior ", "");
      }

      if (itemDto.getFrameType() == Rarity.Rare || StringUtils.isBlank(itemDto.getName())) {
        name = null;
      }
    }

    var baseType = itemDto.getTypeLine();
    if (baseType != null) {

      if (baseType.startsWith("Synthesised ")) {
        baseType = baseType.replace("Synthesised ", "");
      }

    }

    return builder.name(name).baseType(baseType).build();
  }

  public void parseIcon(Wrapper wrapper) throws ItemParseException {
    var icon = wrapper.getItemDto().getIcon();
    var newIcon = ItemUtility.formatIcon(icon);
    wrapper.getItem().setIcon(newIcon);
  }

  public void parseMap(Wrapper wrapper) {
    var base = wrapper.getBase();
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    if (wrapper.getGroupDto() == GroupDto.unique && !itemDto.isIdentified()) {
      log.debug("[A1] {}", itemDto);
      wrapper.discard("Cannot parse unidentified unique map");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Magic) {
      log.debug("[A2] {}", itemDto);
      wrapper.discard("Cannot parse magic maps");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Rare) {
      // todo: actually we can
      log.debug("[A3] {}", itemDto);
      wrapper.discard("Cannot parse rare maps");
      return;
    }

    if (wrapper.getGroupDto() == GroupDto.map) {
      var tier = ItemUtility.extractMapTier(wrapper);
      item.setMapTier(tier);

      var series = ItemUtility.extractMapSeries(wrapper);
      item.setMapSeries(series);
    }

    if (wrapper.getGroupDto() != GroupDto.unique) {
      base.setFrameType(Rarity.Normal.ordinal());
    }
  }

  public void parseGem(Wrapper wrapper) {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    var level = ItemUtility.extractGemLevel(wrapper);
    var quality = ItemUtility.extractGemQuality(wrapper);

    if (wrapper.isDiscard()) {
      return;
    }

    // Accept some quality ranges
    if (quality < 5) {
      quality = 0;
    } else if (quality > 17 && quality < 23) {
      quality = 20;
    } else if (quality != 23) {
      wrapper.discard("Quality is out of range");
      return;
    }

    // Begin the long block that filters out gems based on a number of properties
    if (ItemUtility.isSpecialSupportGem(itemDto)) {
      // Quality doesn't matter for lvl 3 and 4
      if (level > 2) {
        quality = 0;
      }
    } else if (itemDto.getTypeLine().equals("Brand Recall")) {
      if (level <= 2) {
        level = 1;
      } else if (level < 5) {
        wrapper.discard("Level is out of range for Brand Recall");
        return;
      }
    } else {
      // Accept some level ranges
      if (level < 5) {
        level = 1;
      } else if (level < 20) {
        wrapper.discard("Level is out of range for gem");
        return;
      }
    }

    if (itemDto.getIsCorrupted() != null && !itemDto.getIsCorrupted() && (level > 20 || quality > 20)) {
      wrapper.discard("Encountered API bug for gems");
      return;
    }

    item.setGemLevel(level);
    item.setGemQuality(quality);
    item.setGemCorrupted(itemDto.getIsCorrupted());
  }

  public void parseStackSize(Wrapper wrapper) {
    var item = wrapper.getItem();
    var stackSize = ItemUtility.extractMaxStackSize(wrapper);
    item.setStackSize(stackSize);
  }

  public void parseVariant(Wrapper wrapper) {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    var variant = itemVariantService.getVariation(itemDto);
    if (variant.isEmpty()) {
      return;
    }

    item.setVariation(variant.get().getVariation());
  }

}
