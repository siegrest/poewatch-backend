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
@Table(name = "stashes")
public class Stash {

  @Id
  @Column(name = "id", length = 64)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_league")
  private League league;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_account")
  private Account account;

  // todo: rename to row-something
  @Builder.Default
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "stash", fetch = FetchType.LAZY)
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

  @PrePersist
  protected void onCreate() {
    itemCount = items == null ? 0 : items.size();
  }

  @PreUpdate
  protected void onUpdate() {
    updates++;
    itemCount = items == null ? 0 : items.size();
  }

}
