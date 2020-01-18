package watch.poe.app.service.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.repository.ItemRepository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public final class ItemIndexerService {

  @Autowired
  private ItemRepository itemRepository;

  public void save(Wrapper wrapper) {
    var items = wrapper.getBase().getItems();

    if (items.isEmpty()) {
      saveNew(wrapper);
      log.debug("Saved new item {}", wrapper.getItem());
      return;
    }

    var oItem = findMatch(items, wrapper.getItem());
    if (oItem.isPresent()) {
      wrapper.setItem(oItem.get());
      return;
    }

    saveNew(wrapper);
    log.debug("Saved new item {}", wrapper.getItem());
  }

  public Optional<Item> findMatch(Set<Item> haystack, Item needle) {
    if (haystack.isEmpty()) {
      return Optional.empty();
    }

    return haystack.stream()
      .filter(i -> ItemUtility.itemEquals(needle, i))
      .findFirst();
  }

  public void saveNew(Wrapper wrapper) {
    var item = wrapper.getItem();

    item.setBase(wrapper.getBase());
    item.setFound(new Date());

    item = itemRepository.save(item);
    wrapper.setItem(item);
  }

}
