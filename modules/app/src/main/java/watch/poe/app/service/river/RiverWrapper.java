package watch.poe.app.service.river;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import watch.poe.app.domain.Price;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.League;
import watch.poe.persistence.model.Stash;

@Getter
@Setter
@Builder
@ToString
public class RiverWrapper {
  private Stash stash;
  private Item item;
  private Price price;
  private League league;
}
