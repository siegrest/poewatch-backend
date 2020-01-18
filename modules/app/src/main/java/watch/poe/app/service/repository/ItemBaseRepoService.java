package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.ItemBaseRepository;

@Slf4j
@Service
public class ItemBaseRepoService {

  @Autowired
  private ItemBaseRepository itemBaseRepository;

  public ItemBase getOrSave(ItemBase newItemBase) {
    var itemBase = itemBaseRepository.findByCategoryAndGroupAndFrameTypeAndNameAndBaseType(newItemBase.getCategory(),
      newItemBase.getGroup(), newItemBase.getFrameType(), newItemBase.getName(), newItemBase.getBaseType());

    if (itemBase.isEmpty()) {
      return itemBaseRepository.save(newItemBase);
    }

    return itemBase.get();
  }

}
