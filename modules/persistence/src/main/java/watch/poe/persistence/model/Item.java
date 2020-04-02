package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item", schema = "pw")
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ToString.Exclude
  @ManyToOne(optional = false)
  @JoinColumn(name = "item_base_id", nullable = false)
  private ItemBase base;

  @Temporal(TemporalType.TIMESTAMP)
  private Date found;

  @Column(length = 32)
  private String variation;

  @Column(nullable = false)
  private String icon;

  @Column(name = "stack_size")
  private Integer stackSize;
  @Column(name = "item_level")
  private Integer itemLevel;
  @Column(name = "links")
  private Integer links;
  @Column(name = "corrupted")
  private Boolean corrupted;

  @Column(name = "map_tier")
  private Integer mapTier;
  @Column(name = "map_series")
  private Integer mapSeries;

  @Column(name = "gem_level")
  private Integer gemLevel;
  @Column(name = "gem_quality")
  private Integer gemQuality;

  // todo: create new table for these
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
  private Double enchantMin;
  @Column(name = "enchantment_max")
  private Double enchantMax;

}
