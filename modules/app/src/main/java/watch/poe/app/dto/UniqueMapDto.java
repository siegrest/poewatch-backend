package watch.poe.app.dto;

import lombok.Getter;
import watch.poe.persistence.model.item.FrameType;

@Getter
public class UniqueMapDto {
  private String name;
  private String type;
  private FrameType rarity;
}
