package watch.poe.persistence.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account", schema = "pw")
public class Account {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "name", length = 32, nullable = false, unique = true)
  private String name;

  @CreationTimestamp
  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @UpdateTimestamp
  @Column(name = "seen", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Builder.Default
  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  private List<Character> characters = new ArrayList<>();

}
