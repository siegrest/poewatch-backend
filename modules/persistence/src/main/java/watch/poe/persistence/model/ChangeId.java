package watch.poe.persistence.model;

import lombok.*;
import watch.poe.persistence.domain.ChangeIdType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "change_id", schema = "pw")
public class ChangeId {
  @Id
  @Enumerated(value = EnumType.STRING)
  private ChangeIdType type;

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  @Column(length = 64)
  private String value;
}
