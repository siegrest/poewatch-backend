package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
}
