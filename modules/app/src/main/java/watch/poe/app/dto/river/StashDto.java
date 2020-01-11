package watch.poe.app.dto.river;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class StashDto {
    @SerializedName("stash")
    private String stashName;

    private String id, accountName, stashType, lastCharacterName, league;
    private List<ItemDto> items;

    @SerializedName("public")
    private boolean isPublic;
}
