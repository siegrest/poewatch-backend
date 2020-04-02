package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class DateUtility {

  public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public static LocalDateTime parseIso(String date) {
    if (date == null) {
      return null;
    }

    return LocalDateTime.parse(date, formatter);
  }

}
