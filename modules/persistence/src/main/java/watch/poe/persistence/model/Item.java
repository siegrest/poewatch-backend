package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
  name = "items",
  uniqueConstraints = {
    @UniqueConstraint(
      columnNames = {
        "item_base",
        "stack_size",
        "item_level",
        "links",
        "variation",
        "map_tier",
        "map_series",
        "influence_shaper",
        "influence_elder",
        "influence_crusader",
        "influence_redeemer",
        "influence_hunter",
        "influence_warlord",
        "enchantment_min",
        "enchantment_max",
        "gem_level",
        "gem_quality",
        "gem_corrupted"
      }
    )
  }
)
public class Item {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_base", nullable = false)
  private ItemBase base;

  @Column(name = "found", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column(name = "variation", length = 32)
  private String variation;

  @Column(name = "icon", nullable = false)
  private String icon;

  @Column(name = "stack_size")
  private Integer stackSize;
  @Column(name = "item_level")
  private Integer itemLevel;
  @Column(name = "links")
  private Integer links;

  @Column(name = "map_tier")
  private Integer mapTier;
  @Column(name = "map_series")
  private Integer mapSeries;

  @Column(name = "gem_level")
  private Integer gemLevel;
  @Column(name = "gem_quality")
  private Integer gemQuality;
  @Column(name = "gem_corrupted")
  private Boolean gemCorrupted;

  @Column(name = "influence_shaper")
  private Boolean influenceShaper;
  @Column(name = "influence_elder")
  private Boolean influenceElder;
  @Column(name = "influence_crusader")
  private Boolean influenceCrusader;
  @Column(name = "influence_redeemer")
  private Boolean influenceRedeemer;
  @Column(name = "influence_hunter")
  private Boolean influenceHunter;
  @Column(name = "influence_warlord")
  private Boolean influenceWarlord;

  @Column(name = "enchantment_min")
  private Integer enchantMin;
  @Column(name = "enchantment_max")
  private Integer enchantMax;

}
