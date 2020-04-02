package watch.poe.app.dto.statistics;

import lombok.Builder;
import lombok.Getter;
import watch.poe.persistence.model.statistic.StatType;

@Getter
@Builder
public class StatTimer {
    private long startTime;
    private StatType type;
}
