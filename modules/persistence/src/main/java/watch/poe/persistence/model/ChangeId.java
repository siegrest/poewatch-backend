package watch.poe.persistence.model;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import watch.poe.persistence.domain.ChangeIdId;

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
  @Enumerated(value = EnumType.STRING)
  private ChangeIdId id;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  public Date updated;

  @Column(name = "change_id", length = 64)
  private String changeId;

}
