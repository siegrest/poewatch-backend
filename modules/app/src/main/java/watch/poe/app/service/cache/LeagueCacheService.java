package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.League;
import watch.poe.persistence.repository.LeagueRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueCacheService {

  private final LeagueRepository leagueRepository;
  private final List<League> leagues = new ArrayList<>();

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    leagues.addAll(leagueRepository.findAll());
  }

  public Optional<League> get(String name) {
    return leagues.stream()
      .filter(i -> i.getName().equals(name))
      .findFirst();
  }

}
