package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "league", schema = "pw")
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Column(name = "start_time")
    private LocalDateTime start;

    @Column(name = "end_time")
    private LocalDateTime end;

}
