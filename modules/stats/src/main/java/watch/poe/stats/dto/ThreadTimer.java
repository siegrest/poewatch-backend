package watch.poe.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
@Builder
public class ThreadTimer {

  private Thread thread;
  private Set<StatTimer> timers;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ThreadTimer that = (ThreadTimer) o;
    return thread.equals(that.thread);
  }

  @Override
  public int hashCode() {
    return Objects.hash(thread);
  }

}
