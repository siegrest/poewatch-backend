package watch.poe.app.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Date;

@Getter
public class StatCollector {
    private final StatGroupType groupType;
    private final TimeFrame collectionPeriod;
    private final StatType type;

    private Date creationTime;
    private Date insertTime;

    private boolean isValueNull;
    private long count;
    private long sum;

    public StatCollector(@NonNull StatType type, @NonNull StatGroupType groupType, TimeFrame collectionPeriod) {
        this.type = type;
        this.groupType = groupType;
        this.collectionPeriod = collectionPeriod;
        reset();
    }

    public boolean isRecorded() {
        return collectionPeriod != null;
    }

    public boolean hasValues() {
        return count > 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime.getTime() >= collectionPeriod.asMilli();
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
        if (groupType.equals(StatGroupType.COUNT)) {
            return count;
        }

        if (isValueNull) {
            return null;
        }

        if (groupType.equals(StatGroupType.SUM)) {
            return sum;
        }

        if (groupType.equals(StatGroupType.AVG)) {
            return sum / count;
        }

        return sum;
    }

    public void reset() {
        if (collectionPeriod == null) {
            creationTime = new Date(TimeFrame.M_1.getCurrent());
            insertTime = new Date(TimeFrame.M_1.getNext());
        } else {
            creationTime = new Date(collectionPeriod.getCurrent());
            insertTime = new Date(collectionPeriod.getNext());
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
        insertTime = new Date(creationTime.getTime() + collectionPeriod.asMilli());
    }

    public void setCount(long count) {
        this.count = count;
    }
}
