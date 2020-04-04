package watch.poe.app.dto.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.GroupDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.item.ItemDetail;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class ItemWrapper {

  private ItemDetail itemDetail;
  private ItemDto itemDto;
  private CategoryDto categoryDto;
  private GroupDto groupDto;

  private List<DiscardErrorCode> discardReasons;
  private boolean discard;

  public void discard(DiscardErrorCode reason) {
    discardReasons.add(reason);
    discard = true;
  }
}
