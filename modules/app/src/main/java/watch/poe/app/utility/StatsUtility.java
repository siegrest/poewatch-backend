package watch.poe.app.utility;

import watch.poe.app.dto.statistics.StatType;
import watch.poe.app.exception.river.RiverDownloadBasis;

public class StatsUtility {

  public static StatType getErrorType(RiverDownloadBasis basis) {
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

}
