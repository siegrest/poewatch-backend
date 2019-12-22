package watch.poe.app.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class RiverStashDto {
    @SerializedName("stash")
    private String stashName;

    private String id, accountName, stashType, lastCharacterName, league;
    private List<RiverItemDto> items;

    @SerializedName("public")
    private boolean isPublic;
}
