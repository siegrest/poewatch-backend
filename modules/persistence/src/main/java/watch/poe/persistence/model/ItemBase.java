package watch.poe.persistence.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Getter
@Entity
@Builder
@NoArgsConstructor
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

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "base", fetch = FetchType.LAZY)
  private Set<Item> extendedItems;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_category", nullable = false)
  private Category category;
  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_group", nullable = false)
  private Group group;

  @Column(name = "name", nullable = false, length = 128)
  private String name;
  @Column(name = "base_type", length = 64)
  private String baseType;

}
