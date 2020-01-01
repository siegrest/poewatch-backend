package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", length = 32, nullable = false, unique = true)
    private String name;

    @Column(name = "found", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date found;

    @Column(name = "seen", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date seen;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Character> characters;
}
