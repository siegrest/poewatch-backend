package watch.poe.stats.dto;

import lombok.Builder;
import lombok.Getter;
import watch.poe.stats.model.code.StatType;

@Getter
@Builder
public class StatTimer {

  private long startTime;
  private StatType type;

}
