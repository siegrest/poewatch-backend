package watch.poe.persistence.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    public void updateLeagues(List<League> queryLeagues) {
        List<League> repoLeagues = leagueRepository.findAll();

        var newLeagues = saveNewLeagues(repoLeagues, queryLeagues);
        if (!newLeagues.isEmpty()) {
            repoLeagues = leagueRepository.findAll();
        }

        setFlags(repoLeagues, queryLeagues);
    }

    public List<League> saveNewLeagues(List<League> repoLeagues, List<League> queryLeagues) {
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


    public void setFlags(List<League> repoLeagues, List<League> queryLeagues) {
        Predicate<String> filterPredicate = league -> repoLeagues.stream().noneMatch(rl -> rl.getName().equals(league));

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
