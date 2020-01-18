package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.ItemBase;

import java.util.Optional;

public interface ItemBaseRepository extends JpaRepository<ItemBase, Integer> {

//  @Transactional(readOnly = true)
//  @Query("select ChangeId c set c.changeId = :changeId, c.time = current_timestamp where c.name = :name")
//  Optional<ItemBase> find(@Param("name") String name, @Param("changeId") String changeId);

  Optional<ItemBase> findByCategoryAndGroupAndFrameTypeAndNameAndBaseType(Category category, Group group, Integer frameType, String name, String baseType);

}
