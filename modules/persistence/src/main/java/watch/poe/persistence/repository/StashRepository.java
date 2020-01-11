package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Stash;

import java.util.Optional;

public interface StashRepository extends JpaRepository<Stash, String> {

    Optional<Stash> findById(String id);

}
