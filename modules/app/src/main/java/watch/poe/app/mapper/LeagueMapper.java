package watch.poe.app.mapper;

import watch.poe.app.dto.league.LeagueDto;
import watch.poe.app.utility.DateTimeUtility;
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
                .start(DateTimeUtility.parseIsoUtc(leagueDto.getStartAt()))
                .end(DateTimeUtility.parseIsoUtc(leagueDto.getEndAt()))
                .build();
    }

}
