package watch.poe.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

  @Builder.Default
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "stash", fetch = FetchType.LAZY)
  private List<LeagueItemEntry> items = new ArrayList<>();

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
  @Column(name = "item_count", nullable = false)
  private Integer itemCount;

  @PrePersist
  protected void onCreate() {
    updates = 0;
    itemCount = items.size();
  }

  @PreUpdate
  protected void onUpdate() {
    updates++;
    itemCount = items.size();
  }

}
