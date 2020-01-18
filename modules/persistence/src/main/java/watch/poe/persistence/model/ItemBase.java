package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
  name = "items_base",
  uniqueConstraints = {
    @UniqueConstraint(
      columnNames = {
        "name",
        "base_type",
        "frame_type"
      }
    )
  }
)
public class ItemBase {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ToString.Exclude
  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "base", fetch = FetchType.EAGER)
  private Set<Item> items;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_category", nullable = false)
  private Category category;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_group", nullable = false)
  private Group group;

  @Column(name = "name", length = 128)
  private String name;

  @Column(name = "base_type", length = 64)
  private String baseType;

  @Column(name = "frame_type", nullable = false)
  private Integer frameType;

}
