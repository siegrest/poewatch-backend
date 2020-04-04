package watch.poe.persistence.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.item.ItemDetail;

public interface ItemDetailRepository extends JpaRepository<ItemDetail, Integer> {

}
