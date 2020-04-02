package watch.poe.persistence.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.item.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
}
