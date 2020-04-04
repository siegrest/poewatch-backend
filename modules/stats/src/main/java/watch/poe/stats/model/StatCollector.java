package watch.poe.stats.model;

import lombok.*;
import watch.poe.stats.model.code.StatGroupType;
import watch.poe.stats.model.code.StatType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "statistic_collector", schema = "pw")
public class StatCollector {

  @Id
  @Enumerated(EnumType.STRING)
  private StatType type;

  @Column(name = "group_type")
  @Enumerated(EnumType.STRING)
  private StatGroupType groupType;

  @Column
  private Long timespan;

  @Column
  private LocalDateTime start;

  @Column
  private Long count;

  @Column
  private Long sum;

}
