package watch.poe.persistence.model;

import lombok.*;

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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 32, nullable = false, unique = true)
  private String name;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Date seen;

  @Builder.Default
  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  private List<Character> characters = new ArrayList<>();
}
