package watch.poe.persistence.model.changeId;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "change_id", schema = "pw")
public class ChangeId {
  @Id
  @Enumerated(EnumType.STRING)
  private ChangeIdType type;

  @Column(length = 64)
  private String value;

  @Column
  private LocalDateTime time;
}
