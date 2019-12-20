package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "leagues")
public class League {
    @Column(name = "start")
    @Temporal(TemporalType.TIMESTAMP)
    public Date start;

    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    @Column(name = "display", length = 64)
    private String display;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "upcoming", nullable = false)
    private Boolean upcoming;

    @Column(name = "event", nullable = false)
    private Boolean event;

    @Column(name = "hardcore", nullable = false)
    private Boolean hardcore;

    @Column(name = "challenge", nullable = false)
    private Boolean challenge;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date end;

}
