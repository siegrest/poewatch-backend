package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import watch.poe.persistence.model.LeagueItemEntry;

import java.util.List;

public interface LeagueItemEntryRepository extends JpaRepository<LeagueItemEntry, String> {

  @Modifying
  @Query("update LeagueItemEntry set stash = null where id = :id")
  void markStale(@Param("id") String id);

}
