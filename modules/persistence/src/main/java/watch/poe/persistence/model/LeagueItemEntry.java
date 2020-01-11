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
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_stash", nullable = false)
  private Stash stash;

  @Column(name = "item_id", nullable = false, unique = true, length = 64)
  private String itemId;

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

  @Column(name = "stack_size", nullable = false)
  private Integer stackSize;

  @Column(name = "price", nullable = false)
  private Double price;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_price_item", nullable = false)
  private Item priceItem;
}
