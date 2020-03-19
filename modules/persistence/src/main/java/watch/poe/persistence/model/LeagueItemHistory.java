package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "league_item_history", schema = "pw")
public class LeagueItemHistory {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;
    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "time", nullable = false)
    private Timestamp time;

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

}
