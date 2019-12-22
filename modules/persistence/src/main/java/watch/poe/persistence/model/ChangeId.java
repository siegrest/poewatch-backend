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
@Table(name = "change_id")
public class ChangeId {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    @Column(name = "change_id", length = 64)
    private String changeId;

    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date time;

}
