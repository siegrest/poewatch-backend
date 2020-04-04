package watch.poe.stats.dto;

import lombok.Builder;
import lombok.Getter;
import watch.poe.stats.model.code.StatType;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
public class StatTimerDto {

  private Thread thread;
  private LocalDateTime startTime;
  private StatType type;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StatTimerDto that = (StatTimerDto) o;
    return thread.equals(that.thread) &&
      type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(thread, type);
  }

}
