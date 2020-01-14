package watch.poe.app.service.item;

import lombok.Builder;
import lombok.Getter;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;

@Getter
@Builder
class Wrapper {
  private ItemBase base;
  private Item item;
  private ItemDto itemDto;
  private CategoryDto categoryDto;
  private GroupDto groupDto;
}
