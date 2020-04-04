package watch.poe.stats.mapper;

import watch.poe.stats.model.StatCollector;
import watch.poe.stats.model.StatHistory;
import watch.poe.stats.model.code.StatGroupType;

public class StatMapper {

  public static StatHistory toStatHistory(StatCollector collector) {
    return StatHistory.builder()
      .time(collector.getStart())
      .type(collector.getType())
      .value(getValue(collector))
      .build();
  }

  private static Long getValue(StatCollector collector) {
    var groupType = collector.getGroupType();

    if (StatGroupType.COUNT.equals(groupType)) {
      return collector.getCount();
    }

    if (collector.getSum() == null) {
      return null;
    }

    if (StatGroupType.SUM.equals(groupType)) {
      return collector.getSum();
    }

    if (StatGroupType.AVG.equals(groupType)) {
      return collector.getSum() / collector.getCount();
    }

    return collector.getSum();
  }

}
