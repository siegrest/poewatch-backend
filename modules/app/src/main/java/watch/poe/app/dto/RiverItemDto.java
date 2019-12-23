package watch.poe.app.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class RiverItemDto {
    @SerializedName("identified")
    private boolean isIdentified;
    private int itemLevel, frameType;
    @SerializedName("corrupted")
    private Boolean isCorrupted;
    @SerializedName("synthesised")
    private Boolean isSynthesised;
    private String icon, league, id, name, typeLine, note;
    private Integer stackSize;
    private String prophecyText;

    @SerializedName(value = "raceReward", alternate = {"seaRaceReward", "cisRaceReward", "thRaceReward", "RaceReward"})
    private Object raceReward;

    private RiverItemInfluencesDto influences;
    private RiverItemExtendedDto extended;
    private List<RiverItemPropertyDto> properties;
    private List<RiverItemSocketDto> sockets;
    private List<String> explicitMods;
    private List<String> enchantMods;
}
