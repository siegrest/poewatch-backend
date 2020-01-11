package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@Entity
@Table(name = "league_item_entries")
public class LeagueItemEntry {

  @Id
  @Column(name = "id", length = 64)
  private String id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_stash", nullable = false)
  private Stash stash;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_item", nullable = false)
  private Item item;

  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;
  @Column(name = "seen", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Column(name = "updates", nullable = false)
  private Integer updates;

  @Column(name = "stack_size", nullable = true)
  private Integer stackSize;

  @Column(name = "price", nullable = true)
  private Double price;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_price_item", nullable = true)
  private Item priceItem;

}
