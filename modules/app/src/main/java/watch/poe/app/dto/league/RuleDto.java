package watch.poe.app.dto.league;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RuleDto {
    private String id;
    private String name;
    private String description;

    // todo: move to utility package
    public boolean isHardcore() {
        return "Hardcore".equals(name);
    }

    // todo: move to utility package
    public boolean isSolo() {
        return "Solo".equals(name);
    }
}
