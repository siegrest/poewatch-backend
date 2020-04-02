package watch.poe.app.dto.statistics;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
public class StatCollector {

  private final StatType type;
  private LocalDateTime creationTime;
  private LocalDateTime insertTime;
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
    var ms = creationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    return System.currentTimeMillis() - ms >= type.getTimeFrame().asMilli();
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
      creationTime = TimeFrame.M_1.getCurrent();
      insertTime = TimeFrame.M_1.getNext();
    } else {
      creationTime = type.getTimeFrame().getCurrent();
      insertTime = type.getTimeFrame().getNext();
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

  public void setCreationTime(LocalDateTime creationTime) {
    this.creationTime = creationTime;
    insertTime = creationTime.plusNanos(type.getTimeFrame().asMilli() * 1000000);
  }

  public void setCount(long count) {
    this.count = count;
  }

}
