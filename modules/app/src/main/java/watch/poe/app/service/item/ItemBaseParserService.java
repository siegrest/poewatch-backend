package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import watch.poe.app.domain.Rarity;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.CategorizationService;
import watch.poe.persistence.model.ItemBase;

import java.util.Set;

@Component
@Slf4j
public final class ItemBaseParserService {

  @Autowired
  private CategorizationService categorizationService;

  public ItemBase parse(Wrapper wrapper) throws ItemParseException {
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

}
