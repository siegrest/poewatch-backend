package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.persistence.model.ChangeId;

import java.util.Optional;

public interface ChangeIdRepository extends JpaRepository<ChangeId, Integer> {

    Optional<ChangeId> getByName(String name);

    Optional<ChangeId> getById(Integer id);

    @Modifying
    @Transactional
    @Query("update ChangeId c set c.changeId = :changeId, c.time = current_timestamp where c.name = :name")
    void updateByName(@Param("name") String name, @Param("changeId") String changeId);

}
