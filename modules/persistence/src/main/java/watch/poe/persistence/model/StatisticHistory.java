package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@IdClass(StatisticPk.class)
@Table(name = "statistic_history", schema = "pw")
public class StatisticHistory {

    @Id
    @Column(name = "type", length = 64, nullable = false)
    private String type;

    @Id
    @Column(name = "time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Column(name = "value", nullable = false)
    private Long value;

}
