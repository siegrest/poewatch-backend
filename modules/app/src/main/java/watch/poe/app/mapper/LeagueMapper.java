package watch.poe.app.mapper;

import watch.poe.app.domain.LeagueDto;
import watch.poe.app.utility.DateUtility;
import watch.poe.persistence.model.League;

public final class LeagueMapper {

    public static League map(LeagueDto leagueDto) {
        return League.builder()
                .name(leagueDto.getId())
                .display(leagueDto.getId())
                .active(true)
                .upcoming(false)
                .event(leagueDto.isEvent())
                .hardcore(leagueDto.isHardcore())
                .challenge(leagueDto.isChallenge())
                .start(DateUtility.parseIso(leagueDto.getStartAt()))
                .end(DateUtility.parseIso(leagueDto.getEndAt()))
                .build();
    }

}
