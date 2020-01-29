package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.domain.ChangeIdId;
import watch.poe.persistence.model.ChangeId;

public interface ChangeIdRepository extends JpaRepository<ChangeId, ChangeIdId> {
}
