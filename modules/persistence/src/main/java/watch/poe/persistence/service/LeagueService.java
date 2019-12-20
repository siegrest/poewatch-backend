package watch.poe.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    public void updateLeagueData(List<League> activeLeagues) {
        // Map leagues to league name
        List<String> activeLeagueNames = activeLeagues.stream().map(League::getName).collect(Collectors.toList());

        // Update and insert
        leagueRepository.saveAll(activeLeagues);
        leagueRepository.setLeagueFlags(activeLeagueNames);
        leagueRepository.flush();
    }

    public List<League> getAll() {
        return leagueRepository.findAll();
    }

}
