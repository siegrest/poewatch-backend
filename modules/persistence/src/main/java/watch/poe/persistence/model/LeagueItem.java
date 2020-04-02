package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "league_item", schema = "pw")
public class LeagueItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "league_id", nullable = false)
  private League league;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column
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
