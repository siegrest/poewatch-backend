package watch.poe.app.domain;

import lombok.Getter;
import watch.poe.persistence.domain.FrameType;

@Getter
public class UniqueMap {
  private String name;
  private String type;
  private FrameType rarity;
}
