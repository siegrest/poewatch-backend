package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.repository.ItemRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCacheService {

  private final ItemRepository itemRepository;
  private final List<Item> items = new ArrayList<>();

  @PostConstruct
  public void init() {
    items.addAll(itemRepository.findAll());
  }

  public Item getOrSave(Item item) {
    var dbItem = items.stream()
      .filter(i -> ItemUtility.itemEquals(i, item))
      .findFirst();

    if (dbItem.isPresent()) {
      return dbItem.get();
    }

    // todo: necessary? or move to item base builder
    item.setId(null);
    item.setFound(new Date());

    var newItem = itemRepository.save(item);
    items.add(newItem);
    return newItem;
  }

}
