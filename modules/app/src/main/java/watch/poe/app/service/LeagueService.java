package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueService {

  private final LeagueRepository leagueRepository;

    public Optional<League> getByName(String name) {
        return leagueRepository.getByName(name);
    }

}
