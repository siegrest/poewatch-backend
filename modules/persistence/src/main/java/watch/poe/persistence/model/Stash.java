package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "stashes")
public class Stash {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "stash_id", nullable = false, unique = true, length = 64)
    private String stashId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_league", nullable = false)
    private League league;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_account", nullable = false)
    private Account account;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "id")
    private Set<LeagueItemEntry> items;
}
