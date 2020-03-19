package watch.poe.persistence.model;

import lombok.*;
import watch.poe.persistence.domain.FrameType;

import javax.persistence.*;
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
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
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

}
