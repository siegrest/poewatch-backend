package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.LeagueItemEntry;

public interface LeagueItemEntryRepository extends JpaRepository<LeagueItemEntry, Long> {

//  // todo: join stashes and find by stash id
//  @Modifying
//  @Query("update LeagueItemEntry set stash = null where stash.id in :stashIds")
//  void markStaleByStashIds(@Param("stashIds") List<String> stashIds);

}
