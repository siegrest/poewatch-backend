package watch.poe.app.domain.statistics;

import lombok.Builder;
import lombok.Getter;
import watch.poe.app.service.statistics.StatType;

@Getter
@Builder
public class StatTimer {
    private long startTime;
    private StatType type;
}
