package watch.poe.persistence.model;

import lombok.*;
import watch.poe.persistence.model.leagueItem.LeagueItemEntry;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "poe_id")
  private String poeId;

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

  @Column
  private LocalDateTime found;

  @Column
  private LocalDateTime seen;

  @Column
  private Integer updates;

  @Column
  private Boolean stale;

}
