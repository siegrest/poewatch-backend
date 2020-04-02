package watch.poe.persistence.model.statistic;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@IdClass(StatisticPk.class)
@Table(name = "statistic_partial", schema = "pw")
public class StatisticPartial {

    @Id
    @Column(name = "type", length = 32)
    private String type;

    @Id
    @Column
    private LocalDateTime time;

    @Column
    private Long sum;

    @Column
    private Long count;

}
