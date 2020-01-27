package watch.poe.app.domain.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.persistence.model.LeagueItemEntry;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
public class RiverWrapper {
  private Set<LeagueItemEntry> entries;
  private LocalDateTime completionTime;
}
