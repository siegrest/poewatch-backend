package watch.poe.app.service.statistics;

import lombok.Getter;

@Getter
public class StatTimer {
    private long startTime = System.currentTimeMillis();
    private StatType type;

    public StatTimer(StatType type) {
        this.type = type;
    }
}
