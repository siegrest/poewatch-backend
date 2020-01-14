package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.ItemBaseRepository;

import java.util.Set;

@Slf4j
@Service
public class ItemBaseRepoService {

  @Autowired
  private ItemBaseRepository itemBaseRepository;

  public ItemBase getOrSaveBase(Category category, Group group, String name, String baseType) {
    var itemBase = itemBaseRepository.findByCategoryAndGroupAndNameAndBaseType(category, group, name, baseType);
    if (itemBase.isEmpty()) {
      var newItemBase = ItemBase.builder()
        .category(category)
        .group(group)
        .name(name)
        .baseType(baseType)
        .items(Set.of())
        .build();

      return itemBaseRepository.save(newItemBase);
    }

    return itemBase.get();
  }

}
