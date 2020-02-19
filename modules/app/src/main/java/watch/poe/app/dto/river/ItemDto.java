package watch.poe.app.dto.river;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ItemDto {
  private boolean identified;
  private int itemLevel;
  private int frameType;
  private Boolean corrupted;
  private String icon, league, id, name, typeLine, note;
  private Integer stackSize;
  private String prophecyText;
  private Boolean abyssJewel;
  private Boolean synthesised;

  @SerializedName(value = "raceReward", alternate = {"seaRaceReward", "cisRaceReward", "thRaceReward", "RaceReward"})
  private Object raceReward;

  private InfluencesDto influences;
  private ExtendedDto extended;
  private List<PropertyDto> properties;
  private List<SocketDto> sockets;
  private List<String> explicitMods;
  private List<String> enchantMods;
}
