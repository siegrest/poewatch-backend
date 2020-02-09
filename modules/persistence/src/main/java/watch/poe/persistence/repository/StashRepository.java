package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import watch.poe.persistence.model.Stash;

import java.util.List;
import java.util.Optional;

public interface StashRepository extends JpaRepository<Stash, Long> {

  Optional<Stash> findById(Long id);

  @Modifying
//  @Query(nativeQuery = true, value = "update stashes set stale = true where id in :ids")
  @Query("update Stash set stale = true where id in :ids")
  void markStale(@Param("ids") List<Long> ids);

}
