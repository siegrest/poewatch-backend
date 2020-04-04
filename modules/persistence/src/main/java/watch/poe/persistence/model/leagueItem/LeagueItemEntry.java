package watch.poe.persistence.model.leagueItem;

import lombok.*;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.model.item.ItemDetail;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "league_item_entry", schema = "pw")
public class LeagueItemEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stash_id")
  private Stash stash;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private ItemDetail itemDetail;

  @Column
  private LocalDateTime found;

  @Column
  private LocalDateTime seen;

  @Column(name = "updates", nullable = false)
  private Integer updates;

  @Column(name = "stack_size")
  private Integer stackSize;

  @Column(name = "price")
  private Double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "price_item_id")
  private ItemDetail priceItem;

}
