package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class DateTimeUtility {

  private static final long NANOS_IN_MS = 1000000L;

  public static LocalDateTime parseIsoUtc(String time) {
    if (StringUtils.isBlank(time)) {
      return null;
    }

    ZonedDateTime parse = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
      .withZoneSameInstant(ZoneId.systemDefault());

    return parse.toLocalDateTime();
  }

  public static LocalDateTime addMs(LocalDateTime date, long ms) {
    return date.plusNanos(ms * NANOS_IN_MS);
  }

  public static LocalDateTime subMs(LocalDateTime date, long ms) {
    return date.minusNanos(ms * NANOS_IN_MS);
  }

}
