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
@Table(name = "character", schema = "pw")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 32, nullable = false, unique = true)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date found;

    @Temporal(TemporalType.TIMESTAMP)
    private Date seen;
}
