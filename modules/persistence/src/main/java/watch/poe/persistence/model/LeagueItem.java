package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@Entity
@Table(
  name = "league_items",
  uniqueConstraints = {
    @UniqueConstraint(
      columnNames = {
        "fk_league",
        "fk_item_type"
      }
    )
  }
)
public class LeagueItem {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_league", nullable = false)
  private League league;
  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_item", nullable = false)
  private Item item;

  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column(name = "seen", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Column(name = "mean", nullable = false)
  private Double mean;
  @Column(name = "median", nullable = false)
  private Double median;
  @Column(name = "mode", nullable = false)
  private Double mode;
  @Column(name = "min", nullable = false)
  private Double min;
  @Column(name = "max", nullable = false)
  private Double max;

  @Column(name = "total", nullable = false)
  private Integer total;
  @Column(name = "daily", nullable = false)
  private Integer daily;
  @Column(name = "current", nullable = false)
  private Integer current;
  @Column(name = "accepted", nullable = false)
  private Integer accepted;

  // todo: spark / history
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "")
//    private List<> history;
}
