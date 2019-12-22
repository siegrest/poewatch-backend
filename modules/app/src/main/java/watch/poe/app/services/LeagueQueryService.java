package watch.poe.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.config.ApiModuleConfig;
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
    private ApiModuleConfig config;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private GsonService gsonService;

    @Value("${league.fetch.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${league.fetch.cron}")
    public void cycle() {
        log.info("Begin query");

        if (!enabled) {
            return;
        }

        List<LeagueDto> leagues = fetchLeagues();
        if (leagues == null) {
            return;
        }

        leagues.removeIf(LeagueDto::isSolo);
        log.info("Fetched {} valid leagues", leagues.size());

        var mappedLeagues = leagues.stream().map(LeagueMapper::map).collect(Collectors.toList());
        leagueService.updateLeagueData(mappedLeagues);

        log.info("Finish league updates: {}", leagueService.getAll());
    }

    private List<LeagueDto> fetchLeagues() {
        try {
            var leagueJson = HttpUtility.fetch(config.getProperty("league.fetch.api"));
            return gsonService.toList(leagueJson, LeagueDto.class);
        } catch (IOException | NullPointerException ex) {
            log.error("An exception occurred while fetching leagues", ex);
            return null;
        }
    }

}

