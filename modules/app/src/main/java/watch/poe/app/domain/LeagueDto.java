package watch.poe.app.domain;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class LeagueDto {
    private String id;
    private String realm;
    private String description;
    private String registerAt;
    private String url;
    private boolean event, delveEvent, timedEvent, scoreEvent;
    private String startAt, endAt;
    private List<LeagueRuleDto> rules;

    // todo: move to utility package
    public boolean isHardcore() {
        return rules != null && rules.stream().anyMatch(LeagueRuleDto::isHardcore);
    }

    // todo: move to utility package
    public boolean isSolo() {
        return rules != null && rules.stream().anyMatch(LeagueRuleDto::isSolo);
    }

    // todo: move to utility package
    public boolean isChallenge() {
        return !event && !"Standard".equals(id) && !"Hardcore".equals(id);
    }
}


