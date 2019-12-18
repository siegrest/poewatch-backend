package watch.poe.persistence.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(
        name = "item_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "name",
                                "base_type",
                                "frame_type",
                                "stack_size",
                                "item_level",
                                "links",
                                "fk_variation",
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
//@ToString
public class ItemType {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer id;

    @Column(name = "found", nullable = false)
    private Timestamp found;
    @Column(name = "reindex", nullable = false)
    private Boolean reindex;

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
    @Column(name = "frame_type", nullable = false)
    private Integer frameType;

    @Column(name = "stack_size")
    private Integer stackSize;
    @Column(name = "item_level")
    private Integer itemLevel;
    @Column(name = "links")
    private Integer links;
    @Column(name = "icon", nullable = false)
    private String icon;

    @ManyToOne
    @JoinColumn(name = "fk_variation")
    private ItemTypeVariation variation;

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

    @PrePersist
    public void prePersist() {
//        if (found == null) {
//            found =
//        }
    }
}
