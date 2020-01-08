package watch.poe.app.utility;

import watch.poe.app.service.statistics.StatType;

import java.util.regex.Pattern;

public class StatsUtility {

  private static final Pattern exceptionPattern5xx = Pattern.compile("^.+ 5\\d\\d .+$");
  private static final Pattern exceptionPattern4xx = Pattern.compile("^.+ 4\\d\\d .+$");

  public static StatType getErrorType(Exception ex) {
    if (ex.getMessage().contains("Read timed out")) {
      return StatType.COUNT_API_ERRORS_READ_TIMEOUT;
    }

    if (ex.getMessage().contains("connect timed out")) {
      return StatType.COUNT_API_ERRORS_CONNECT_TIMEOUT;
    }

    if (ex.getMessage().contains("Connection reset")) {
      return StatType.COUNT_API_ERRORS_CONN_RESET;
    }

    if (exceptionPattern5xx.matcher(ex.getMessage()).matches()) {
      return StatType.COUNT_API_ERRORS_5XX;
    }

    if (exceptionPattern4xx.matcher(ex.getMessage()).matches()) {
      return StatType.COUNT_API_ERRORS_4XX;
    }

    return null;
  }

}
