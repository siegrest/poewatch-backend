package watch.poe.app.domain.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatTimer {
    private long startTime;
    private StatType type;
}
