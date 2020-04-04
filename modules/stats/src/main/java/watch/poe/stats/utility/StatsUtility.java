package watch.poe.stats.utility;


import watch.poe.persistence.model.code.RiverErrorCode;
import watch.poe.stats.model.StatCollector;
import watch.poe.stats.model.code.StatType;

import java.time.LocalDateTime;

public class StatsUtility {

  public static StatType getErrorType(RiverErrorCode basis) {
    switch (basis) {
      case READ_TIMEOUT:
        return StatType.COUNT_API_ERRORS_READ_TIMEOUT;
      case CONNECT_TIMEOUT:
        return StatType.COUNT_API_ERRORS_CONNECT_TIMEOUT;
      case CONNECTION_RESET:
        return StatType.COUNT_API_ERRORS_CONN_RESET;
      case HTTP_5XX:
        return StatType.COUNT_API_ERRORS_5XX;
      case HTTP_4XX:
        return StatType.COUNT_API_ERRORS_4XX;
      case UNKNOWN:
      default:
        return null;
    }
  }

  public static boolean hasValues(StatCollector collector) {
    return collector.getCount() > 0;
  }

  public static boolean isExpired(StatCollector collector) {
    LocalDateTime endTime = LocalDateTime.now().plusNanos(collector.getTimespan() * 1000000L);
    return LocalDateTime.now().isAfter(endTime);
  }

}
