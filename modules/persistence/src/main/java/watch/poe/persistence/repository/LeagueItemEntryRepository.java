package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import watch.poe.persistence.model.LeagueItemEntry;

import java.util.List;

public interface LeagueItemEntryRepository extends JpaRepository<LeagueItemEntry, String> {

  @Query(nativeQuery = true, value = "update league_item_entries set fk_stash = null where id in :ids")
  void markStale(@Param("ids") List<String> ids);

}
