package watch.poe.app.domain.statistics;

import lombok.Getter;

import java.util.Date;

@Getter
public class StatCollector {
    private final StatType type;
    private Date creationTime;
    private Date insertTime;
    private boolean isValueNull;
    private long count;
    private long sum;

    public StatCollector(StatType statType) {
        this.type = statType;
        reset();
    }

    public boolean isRecorded() {
        return type.getTimeFrame() != null;
    }

    public boolean hasValues() {
        return count > 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime.getTime() >= type.getTimeFrame().asMilli();
    }

    public void addValue(Long val) {
        if (val == null) {
            isValueNull = true;
        } else {
            sum += val;
        }

        count++;
    }

    public Long getValue() {
        if (type.getStatGroupType().equals(StatGroupType.COUNT)) {
            return count;
        }

        if (isValueNull) {
            return null;
        }

        if (type.getStatGroupType().equals(StatGroupType.SUM)) {
            return sum;
        }

        if (type.getStatGroupType().equals(StatGroupType.AVG)) {
            return sum / count;
        }

        return sum;
    }

    public void reset() {
        if (type.getTimeFrame() == null) {
            creationTime = new Date(TimeFrame.M_1.getCurrent());
            insertTime = new Date(TimeFrame.M_1.getNext());
        } else {
            creationTime = new Date(type.getTimeFrame().getCurrent());
            insertTime = new Date(type.getTimeFrame().getNext());
        }

        isValueNull = false;
        count = 0;
        sum = 0;
    }

    public void setSum(Long sum) {
        if (sum == null) {
            isValueNull = true;
        } else {
            this.sum = sum;
        }
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        insertTime = new Date(creationTime.getTime() + type.getTimeFrame().asMilli());
    }

    public void setCount(long count) {
        this.count = count;
    }
}
