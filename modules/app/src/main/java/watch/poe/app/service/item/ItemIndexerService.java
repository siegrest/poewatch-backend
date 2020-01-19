package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.CategoryRepository;
import watch.poe.persistence.repository.GroupRepository;
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
  private GroupRepository groupRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  public Item index(Wrapper wrapper) {
    var category = getOrSaveCategory(wrapper);
    var group = getOrSaveGroup(wrapper);

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

  public Category getOrSaveCategory(Wrapper wrapper) {
    var itemCategory = wrapper.getBase().getCategory();
    var category = categoryRepository.getByName(itemCategory.getName());

    if (category.isEmpty()) {
      itemCategory = categoryRepository.save(itemCategory);
      log.info("Added category to database: {}", itemCategory);
      return itemCategory;
    }

    return category.get();
  }

  public Group getOrSaveGroup(Wrapper wrapper) {
    var itemGroup = wrapper.getBase().getGroup();
    var group = groupRepository.getByName(itemGroup.getName());

    if (group.isEmpty()) {
      itemGroup = groupRepository.save(itemGroup);
      log.info("Added group to database: {}", itemGroup);
      return itemGroup;
    }

    return group.get();
  }

}
