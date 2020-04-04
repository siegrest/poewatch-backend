package watch.poe.stats.model;

import lombok.*;
import watch.poe.stats.model.code.StatType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "statistic_history", schema = "pw")
public class StatHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING)
  private StatType type;

  @Column
  private LocalDateTime time;

  @Column
  private Long value;

}
