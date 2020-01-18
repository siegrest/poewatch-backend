package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.CategorizationService;
import watch.poe.app.service.resource.ItemVariantService;
import watch.poe.app.utility.ItemUtility;

@Service
@Slf4j
public final class ItemParserService {

  @Autowired
  private CategorizationService categorizationService;
  @Autowired
  private ItemVariantService itemVariantService;
  @Autowired
  private ItemBaseService itemBaseService;

  public void parse(Wrapper wrapper) throws ItemParseException {
    var itemDto = wrapper.getItemDto();

    var categoryDto = categorizationService.determineCategoryDto(itemDto);
    wrapper.setCategoryDto(categoryDto);

    var groupDto = categorizationService.determineGroupDto(itemDto, categoryDto);
    wrapper.setGroupDto(groupDto);

    var base = itemBaseService.getOrSave(wrapper);
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
      log.info("[A1] {}", itemDto);
      wrapper.discard("Cannot parse unidentified unique map");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Magic) {
      log.info("[A2] {}", itemDto);
      wrapper.discard("Cannot parse magic maps");
      return;
    }
    if (wrapper.getGroupDto() == GroupDto.map && itemDto.getFrameType() == Rarity.Rare) {
      // todo: actually we can
      log.info("[A3] {}", itemDto);
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
