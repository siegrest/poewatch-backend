package watch.poe.persistence.model.item;

import lombok.*;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "item_base", schema = "pw")
public class ItemBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ToString.Exclude
  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "base", fetch = FetchType.EAGER)
  private List<Item> items;

  @ManyToOne(optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "name", length = 128)
  private String name;

  @Column(name = "base_type", length = 64)
  private String baseType;

  @Enumerated(EnumType.STRING)
  @Column(name = "frame_type", nullable = false)
  private FrameType frameType;

  @Column
  private LocalDateTime found;

}
