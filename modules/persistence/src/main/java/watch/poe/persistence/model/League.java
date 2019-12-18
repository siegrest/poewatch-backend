package watch.poe.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "leagues")
public class League {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "start")
    private Timestamp start;

    @Column(name = "end")
    private Timestamp end;
}
