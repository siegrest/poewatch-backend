package watch.poe.app.dto.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.app.dto.CategoryDto;
import watch.poe.app.dto.river.ItemDto;

@Getter
@Setter
@Builder
@ToString
public class CategoryWrapper {
  private ItemDto itemDto;
  private CategoryDto categoryDto;
  private String apiCategory;
  private String apiGroup;
  private String iconCategory;
}
