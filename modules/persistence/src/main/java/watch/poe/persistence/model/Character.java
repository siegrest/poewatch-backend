package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date found;

    @Column(name = "seen", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date seen;
}
