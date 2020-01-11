package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.util.Optional;

@Slf4j
@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    public Optional<League> getByName(String name) {
        return leagueRepository.getByName(name);
    }

}
