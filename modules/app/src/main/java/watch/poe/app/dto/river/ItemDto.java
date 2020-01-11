package watch.poe.app.dto.river;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ItemDto {
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

    private InfluencesDto influences;
    private ExtendedDto extended;
    private List<PropertyDto> properties;
    private List<SocketDto> sockets;
    private List<String> explicitMods;
    private List<String> enchantMods;
}
