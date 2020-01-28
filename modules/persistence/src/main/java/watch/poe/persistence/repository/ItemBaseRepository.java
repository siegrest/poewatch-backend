package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.domain.FrameType;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.ItemBase;

import java.util.Optional;

public interface ItemBaseRepository extends JpaRepository<ItemBase, Integer> {

  Optional<ItemBase> findByCategoryAndGroupAndFrameTypeAndNameAndBaseType(Category category, Group group, FrameType frameType, String name, String baseType);

  Optional<ItemBase> findByFrameTypeAndBaseType(FrameType frameType, String baseType);

}
