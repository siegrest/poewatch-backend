package watch.poe.app.domain.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.persistence.model.LeagueItemEntry;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class StashWrapper {

  private long id;
  private String league;
  private String account;
  private String character;
  private List<LeagueItemEntry> entries;

}
