package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_league", nullable = false)
    private League league;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_account", nullable = false)
    private Account account;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "stash", fetch = FetchType.LAZY)
    private Set<LeagueItemEntry> items;

    @Column(name = "found", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date found;

    @Column(name = "seen", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date seen;

    @Column(name = "updates", nullable = false)
    private Integer updates;

}
