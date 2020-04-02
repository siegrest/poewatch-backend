package watch.poe.stats.model;

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
@Table(name = "statistic_history", schema = "pw")
public class StatisticHistory {

  @Id
  @Column(name = "type", length = 64, nullable = false)
  private String type;

  @Id
  @Column
  private LocalDateTime time;

  @Column
  private Long value;

}
