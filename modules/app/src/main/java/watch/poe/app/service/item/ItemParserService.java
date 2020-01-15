package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.exception.InvalidIconException;
import watch.poe.app.exception.ItemDiscardException;
import watch.poe.app.service.CategorizationService;
import watch.poe.app.service.repository.ItemBaseRepoService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;

import java.util.Set;

@Component
@Slf4j
public final class ItemParserService {

  @Autowired
  private CategorizationService categorizationService;
  @Autowired
  private ItemBaseRepoService itemBaseRepoService;
  @Autowired
  private ItemVariantService itemVariantService;

  public Item parse(ItemDto itemDto) throws ItemDiscardException {
    var categoryDto = categorizationService.determineCategoryDto(itemDto);
    var groupDto = categorizationService.determineGroupDto(itemDto, categoryDto);
    var base = parseBase(itemDto, categoryDto, groupDto);
    var wrapper = Wrapper.builder()
      .categoryDto(categoryDto)
      .groupDto(groupDto)
      .itemDto(itemDto)
      .item(Item.builder().build())
      .base(base)
      .build();

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

    if (ItemUtility.isLinkable(itemDto, categoryDto, groupDto)) {
      var links = ItemUtility.extractLinks(wrapper.getItemDto());
      wrapper.getItem().setLinks(links);
    }

    if (itemVariantService.hasVariation(wrapper.getItemDto())) {
      parseVariant(wrapper);
    }

    return wrapper.getItem();
  }

  public ItemBase parseBase(ItemDto itemDto, CategoryDto categoryDto, GroupDto groupDto) {
    var category = categorizationService.categoryDtoToCategory(categoryDto);
    var group = categorizationService.groupDtoToGroup(groupDto);

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

  public void parseIcon(Wrapper wrapper) throws ItemDiscardException {
    try {
      var icon = wrapper.getItemDto().getIcon();
      var newIcon = ItemUtility.formatIcon(icon);
      wrapper.getItem().setIcon(newIcon);
    } catch (InvalidIconException ex) {
      throw new ItemDiscardException(ex);
    }
  }

  public void parseMap(Wrapper wrapper) throws ItemDiscardException {
    var base = wrapper.getBase();
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    if (wrapper.getGroupDto() == GroupDto.unique && !itemDto.isIdentified()) {
      throw new ItemDiscardException("Cannot parse unidentified unique map");
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Magic) {
      throw new ItemDiscardException("Cannot parse magic maps");
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Rare) {
      // todo: actually we can
      throw new ItemDiscardException("Cannot parse rare maps");
    }

    if (wrapper.getGroupDto() == GroupDto.map) {
      var tier = ItemUtility.extractMapTier(itemDto);
      item.setMapTier(tier);

      var series = ItemUtility.extractMapSeries(itemDto);
      item.setMapSeries(series);
    }

    if (wrapper.getGroupDto() != GroupDto.unique) {
      base.setFrameType(Rarity.Normal.ordinal());
    }

  }

  public void parseGem(Wrapper wrapper) throws ItemDiscardException {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();

    var level = ItemUtility.extractGemLevel(itemDto);
    var quality = ItemUtility.extractGemQuality(itemDto);

    // Accept some quality ranges
    if (quality < 5) {
      quality = 0;
    } else if (quality > 17 && quality < 23) {
      quality = 20;
    } else if (quality != 23) {
      throw new ItemDiscardException("Quality is out of range");
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
        throw new ItemDiscardException("Level is out of range for Brand Recall");
      }
    } else {
      // Accept some level ranges
      if (level < 5) {
        level = 1;
      } else if (level < 20) {
        throw new ItemDiscardException("Level is out of range for gem");
      }
    }

    if (itemDto.getIsCorrupted() != null && !itemDto.getIsCorrupted() && (level > 20 || quality > 20)) {
      throw new ItemDiscardException("Encountered API bug for gems");
    }

    item.setGemLevel(level);
    item.setGemQuality(quality);
    item.setGemCorrupted(itemDto.getIsCorrupted());
  }

  public void parseStackSize(Wrapper wrapper) throws ItemDiscardException {
    var item = wrapper.getItem();
    var itemDto = wrapper.getItemDto();
    var stackSize = ItemUtility.extractMaxStackSize(itemDto);
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
