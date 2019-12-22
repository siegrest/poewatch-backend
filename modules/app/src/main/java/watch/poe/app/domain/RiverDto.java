package watch.poe.app.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class RiverDto {
    @SerializedName("next_change_id")
    private String nextChangeId;
    private List<RiverStashDto> stashes;
}
