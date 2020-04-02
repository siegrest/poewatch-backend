package watch.poe.app.dto.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.DiscardBasis;
import watch.poe.app.dto.GroupDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.persistence.model.Item;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class ItemWrapper {
  private Item item;
  private ItemDto itemDto;
  private CategoryDto categoryDto;
  private GroupDto groupDto;

  private List<DiscardBasis> discardReasons;
  private boolean discard;

  public void discard(DiscardBasis reason) {
    discardReasons.add(reason);
    discard = true;
  }
}
