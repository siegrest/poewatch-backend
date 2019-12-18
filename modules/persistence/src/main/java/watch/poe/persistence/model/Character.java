package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "characters")
public class Character {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_account", nullable = false)
    private Account account;

    @Column(name = "name", length = 32, nullable = false, unique = true)
    private String name;

    @Column(name = "found", nullable = false)
    private Timestamp found;

    @Column(name = "seen", nullable = false)
    private Timestamp seen;
}
