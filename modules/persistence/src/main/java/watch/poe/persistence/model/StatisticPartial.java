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
@Table(name = "statistic_partial")
public class StatisticPartial {

    @Id
    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Id
    @Column(name = "time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Column(name = "sum", nullable = false)
    private Long sum;

    @Column(name = "count", nullable = false)
    private Long count;

}
