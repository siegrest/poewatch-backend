package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.LeagueItemEntry;

public interface LeagueItemEntryRepository extends JpaRepository<LeagueItemEntry, String> {
}
