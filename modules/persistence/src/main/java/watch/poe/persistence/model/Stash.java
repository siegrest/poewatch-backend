package watch.poe.persistence.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stash", schema = "pw")
public class Stash {

  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "league_id")
  private League league;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  // todo: rename to row-something
  @Builder.Default
  @OneToMany(mappedBy = "stash", fetch = FetchType.LAZY)
  private List<LeagueItemEntry> items = new ArrayList<>();

  @Builder.Default
  @CreationTimestamp
  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found = new Date();

  @Builder.Default
  @UpdateTimestamp
  @Column(name = "seen", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen = new Date();

  @Column(name = "updates", nullable = false)
  private int updates;
  @Column(name = "item_count", nullable = false)
  private int itemCount;

  @Column(name = "stale")
  private Boolean stale;

  @PrePersist
  protected void onCreate() {
    itemCount = items == null ? 0 : items.size();
    stale = false;
  }

  @PreUpdate
  protected void onUpdate() {
    updates++;
    itemCount = items == null ? 0 : items.size();
    stale = itemCount > 0;
  }

}
