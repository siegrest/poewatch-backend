package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.League;

public interface LeagueRepository extends JpaRepository<League, Integer> {

}
