package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.LeagueDto;
import watch.poe.app.mapper.LeagueMapper;
import watch.poe.app.utility.HttpUtility;
import watch.poe.persistence.service.LeagueService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeagueQueryService {

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private GsonService gsonService;

    @Value("${league.fetch.enabled}")
    private boolean enabled;
    @Value("${league.fetch.url}")
    private String endpointUrl;

    @Scheduled(cron = "${league.fetch.cron}")
    public void cycle() {
        if (!enabled) {
            return;
        }

        log.info("Begin query");

        List<LeagueDto> leagues = fetchLeagues();
        if (leagues == null) {
            return;
        }

        leagues.removeIf(LeagueDto::isSolo);

        var mappedLeagues = leagues.stream().map(LeagueMapper::map).collect(Collectors.toList());
        leagueService.updateLeagues(mappedLeagues);

        log.info("End query");
    }

    private List<LeagueDto> fetchLeagues() {
        try {
            var leagueJson = HttpUtility.fetch(endpointUrl);
            return gsonService.toList(leagueJson, LeagueDto.class);
        } catch (IOException ex) {
            log.error("An exception occurred while fetching leagues", ex);
            return null;
        }
    }

}

