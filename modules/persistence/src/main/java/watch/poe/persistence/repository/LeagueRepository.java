package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.persistence.model.League;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Integer> {

    Optional<League> getByName(String name);

    List<League> getAllByActive(boolean active);

    @Modifying
    @Transactional
    @Query("update League l set l.active = (l.name in :names), l.upcoming = (l.name not in :names)")
    void setLeagueFlags(@Param("names") List<String> leagueNames);

}
