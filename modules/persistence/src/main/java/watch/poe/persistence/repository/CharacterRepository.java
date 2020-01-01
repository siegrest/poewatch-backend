package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Character;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    Character findByName(String name);

}
