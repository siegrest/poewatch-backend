package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.ItemBaseRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemBaseCacheService {

  private final ItemBaseRepository itemBaseRepository;
  private final List<ItemBase> itemBases = new ArrayList<>();

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    itemBases.addAll(itemBaseRepository.findAll());
  }

  public ItemBase getOrSave(ItemBase base) {
    var dbBase = itemBases.stream()
      .filter(b -> ItemUtility.itemBaseEquals(b, base))
      .findFirst();

    if (dbBase.isPresent()) {
      return dbBase.get();
    }

    // todo: necessary? or move to item base builder
    base.setItems(List.of());
    base.setId(null);

    var newBase = itemBaseRepository.save(base);
    itemBases.add(newBase);
    return newBase;
  }

}
