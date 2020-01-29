package watch.poe.persistence.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @CreationTimestamp
  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @UpdateTimestamp
  @Column(name = "seen", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Column(name = "updates", nullable = false)
  private Integer updates;

  @Column(name = "stack_size")
  private Integer stackSize;

  @Column(name = "price")
  private Double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_price_item")
  private Item priceItem;

  @PrePersist
  protected void onCreate() {
    updates = 0;
  }

  @PreUpdate
  protected void onUpdate() {
    updates++;
  }

}
