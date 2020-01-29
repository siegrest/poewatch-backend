package watch.poe.app.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.service.cache.CategoryCacheService;
import watch.poe.app.service.cache.GroupCacheService;
import watch.poe.app.service.cache.ItemBaseCacheService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.repository.ItemBaseRepository;
import watch.poe.persistence.repository.ItemRepository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemIndexerService {

  private final CategoryCacheService categoryService;
  private final GroupCacheService groupService;
  private final ItemBaseCacheService itemBaseCacheService;

  private final ItemRepository itemRepository;
  private final ItemBaseRepository itemBaseRepository;

  public Item index(Item item) {
    var base = item.getBase();

    var category = categoryService.get(base.getCategory().getName());
    if (category.isEmpty()) {
      // todo: custom exception
      throw new RuntimeException(String.format("Expected to find category '%s'", base.getCategory().getName()));
    } else {
      base.setCategory(category.get());
    }

    var group = groupService.get(base.getGroup().getName());
    if (group.isEmpty()) {
      // todo: custom exception
      throw new RuntimeException(String.format("Expected to find group '%s'", base.getCategory().getName()));
    } else {
      base.setGroup(group.get());
    }

    var itemBase = itemBaseCacheService.getOrSave(base);
    item.setBase(itemBase);

    var items = itemBase.getItems();

    if (items.isEmpty()) {
      item = saveNewItem(item);
      log.debug("Saved new item {}", item);
      return item;
    }

    var oItem = findMatch(items, item);
    if (oItem.isPresent()) {
      return oItem.get();
    }

    item = saveNewItem(item);
    log.debug("Saved new item {}", item);
    return item;
  }


  private Item saveNewItem(Item item) {
    item.setFound(new Date());
    return itemRepository.save(item);
  }

  private Optional<Item> findMatch(Set<Item> haystack, Item needle) {
    if (haystack.isEmpty()) {
      return Optional.empty();
    }

    return haystack.stream()
      .filter(i -> ItemUtility.itemEquals(needle, i))
      .findFirst();
  }

}
