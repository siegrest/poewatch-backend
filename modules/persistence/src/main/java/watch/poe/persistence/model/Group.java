package watch.poe.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", length = 32, nullable = false, unique = true)
    private String name;

    @Column(name = "display", length = 32, nullable = false)
    private String display;
}
