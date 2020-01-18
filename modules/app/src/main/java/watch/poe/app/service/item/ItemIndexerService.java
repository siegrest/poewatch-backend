package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.service.CategorizationService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.ItemBaseRepository;
import watch.poe.persistence.repository.ItemRepository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public final class ItemIndexerService {

  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private ItemBaseRepository itemBaseRepository;
  @Autowired
  private CategorizationService categorizationService;

  public Item index(Wrapper wrapper) {
    var category = categorizationService.getOrSaveCategory(wrapper);
    var group = categorizationService.getOrSaveGroup(wrapper);

    var base = wrapper.getBase();
    base.setCategory(category);
    base.setGroup(group);

    var itemBase = getOrSaveBase(wrapper);
    wrapper.setBase(itemBase);
    var items = itemBase.getItems();

    if (items.isEmpty()) {
      var item = saveNewItem(wrapper);
      log.debug("Saved new item {}", item);
      return item;
    }

    var oItem = findMatch(items, wrapper.getItem());
    if (oItem.isPresent()) {
      return oItem.get();
    }

    var item = saveNewItem(wrapper);
    log.debug("Saved new item {}", item);
    return item;
  }

  public ItemBase getOrSaveBase(Wrapper wrapper) {
    var newItemBase = wrapper.getBase();

    var existingItemBase = itemBaseRepository.findByCategoryAndGroupAndFrameTypeAndNameAndBaseType(newItemBase.getCategory(),
      newItemBase.getGroup(), newItemBase.getFrameType(), newItemBase.getName(), newItemBase.getBaseType());

    if (existingItemBase.isEmpty()) {
      newItemBase = itemBaseRepository.save(newItemBase);
      log.debug("Saved new item base {}", newItemBase);
      return newItemBase;
    }

    return existingItemBase.get();
  }

  public Item saveNewItem(Wrapper wrapper) {
    var item = wrapper.getItem();

    item.setBase(wrapper.getBase());
    item.setFound(new Date());

    return itemRepository.save(item);
  }

  public Optional<Item> findMatch(Set<Item> haystack, Item needle) {
    if (haystack.isEmpty()) {
      return Optional.empty();
    }

    return haystack.stream()
      .filter(i -> ItemUtility.itemEquals(needle, i))
      .findFirst();
  }

}
