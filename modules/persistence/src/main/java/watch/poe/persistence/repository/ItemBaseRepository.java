package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.ItemBase;

public interface ItemBaseRepository extends JpaRepository<ItemBase, Integer> {
}
