package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class DateTimeUtility {

  public static LocalDateTime parseIsoUtc(String time) {
    if (StringUtils.isBlank(time)) {
      return null;
    }

    ZonedDateTime parse = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
      .withZoneSameInstant(ZoneId.systemDefault());

    return parse.toLocalDateTime();
  }

}
