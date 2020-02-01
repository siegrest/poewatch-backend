package watch.poe.app.domain.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.persistence.model.Stash;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class RiverWrapper {
  private List<Stash> stashes;
  private LocalDateTime completionTime;
  private String job;
}
