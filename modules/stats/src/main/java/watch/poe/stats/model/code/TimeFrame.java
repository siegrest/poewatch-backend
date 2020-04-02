package watch.poe.stats.model.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum TimeFrame {
  S_60(60000L),
  M_10(600000L),
  M_30(1800000L),
  M_60(3600000L),
  H_12(43200000L),
  H_24(86400000L);

  private final long ms;

  /**
   * Gets milliseconds from the start until the TimeFrame
   */
  public LocalDateTime getCurrent() {
    var val = (System.currentTimeMillis() / ms) * ms;
    return Instant.ofEpochMilli(val)
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime();
  }

  /**
   * Gets milliseconds from the start until the next TimeFrame
   */
  public LocalDateTime getNext() {
    var val = (System.currentTimeMillis() / ms + 1) * ms;
    return Instant.ofEpochMilli(val)
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime();
  }
}
