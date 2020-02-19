package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Group;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {

  Optional<Group> getByName(String name);

}
