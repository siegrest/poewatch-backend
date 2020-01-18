package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.league.LeagueDto;
import watch.poe.app.mapper.LeagueMapper;
import watch.poe.app.utility.HttpUtility;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeagueQueryService {

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private GsonService gsonService;

    @Value("${league.fetch.enabled}")
    private boolean enabled;
    @Value("${league.fetch.url}")
    private String endpointUrl;

    @Scheduled(cron = "${league.fetch.cron}")
    @EventListener(ApplicationStartedEvent.class)
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
        updateLeagues(mappedLeagues);

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

    private void updateLeagues(List<League> queryLeagues) {
        List<League> repoLeagues = leagueRepository.findAll();

        var newLeagues = saveNewLeagues(repoLeagues, queryLeagues);
        if (!newLeagues.isEmpty()) {
            repoLeagues = leagueRepository.findAll();
        }

        setFlags(repoLeagues, queryLeagues);
    }

    private List<League> saveNewLeagues(List<League> repoLeagues, List<League> queryLeagues) {
        Predicate<String> filterPredicate = league -> repoLeagues.stream().noneMatch(rl -> rl.getName().equals(league));

        var newLeagues = queryLeagues.stream()
          .filter(queryLeague -> filterPredicate.test(queryLeague.getName()))
          .collect(Collectors.toList());

        if (newLeagues.isEmpty()) {
            log.info("No new leagues found");
            return newLeagues;
        }

        log.info("Found new leagues: {}", newLeagues);
        return leagueRepository.saveAll(newLeagues);
    }

    private void setFlags(List<League> repoLeagues, List<League> queryLeagues) {
        Predicate<String> filterPredicate = league -> repoLeagues.stream().noneMatch(rl -> rl.getName().equals(league));

        // todo: fix first leagues set active=false
        repoLeagues.forEach(rLeague -> {
            var matchingLeague = queryLeagues.stream().filter(qLeague -> filterPredicate.test(qLeague.getName())).findFirst();
            if (matchingLeague.isPresent()) {
                rLeague.setActive(true);
                rLeague.setUpcoming(false);
            } else {
                rLeague.setActive(false);
            }
        });

        leagueRepository.saveAll(repoLeagues);
    }

}

