package watch.poe.app.domain.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class RiverWrapper {
  private List<StashWrapper> stashes;
  private LocalDateTime completionTime;
  private String job;
}
