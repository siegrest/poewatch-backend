package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.League;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Integer> {

    Optional<League> getByName(String name);

    List<League> getAllByActiveTrueOrUpcomingTrue();

    List<League> getAllByNameIn(List<String> leagueNames);

    List<League> getAllByUpcoming(boolean upcoming);

}
