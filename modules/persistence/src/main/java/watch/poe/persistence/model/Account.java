package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private Set<Character> characters;

    @Column(name = "name", length = 32, nullable = false, unique = true)
    private String name;

    @Column(name = "found", nullable = false)
    private Timestamp found;

    @Column(name = "seen", nullable = false)
    private Timestamp seen;
}
