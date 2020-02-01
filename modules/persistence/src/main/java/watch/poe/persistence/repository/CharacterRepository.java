package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Character;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

  Optional<Character> findByName(String name);

}
