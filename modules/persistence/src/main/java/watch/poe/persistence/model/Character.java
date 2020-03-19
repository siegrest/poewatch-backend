package watch.poe.persistence.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", length = 32, nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    @Column(name = "found", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date found;

    @UpdateTimestamp
    @Column(name = "seen", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date seen;
}
