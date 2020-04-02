package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.changeId.ChangeIdType;
import watch.poe.persistence.model.changeId.ChangeId;

public interface ChangeIdRepository extends JpaRepository<ChangeId, ChangeIdType> {
}
