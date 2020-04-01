package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "league_item_entry", schema = "pw")
public class LeagueItemEntry {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stash_id")
  private Stash stash;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Column(name = "updates", nullable = false)
  private Integer updates;

  @Column(name = "stack_size")
  private Integer stackSize;

  @Column(name = "price")
  private Double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "price_item_id")
  private Item priceItem;

}
