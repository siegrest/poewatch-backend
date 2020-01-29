package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.ItemBase;
import watch.poe.persistence.repository.ItemBaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemBaseCacheService {

  private final ItemBaseRepository itemBaseRepository;
  private final List<ItemBase> itemBases = new ArrayList<>();

  @EventListener(ApplicationStartedEvent.class)
  public void init() {
    itemBases.addAll(itemBaseRepository.findAll());
  }

  public ItemBase getOrSave(ItemBase base) {
    var dbBase = itemBases.stream()
      .filter(b -> b.getCategory().equals(base.getCategory()))
      .filter(b -> b.getGroup().equals(base.getGroup()))
      .filter(b -> b.getFrameType().equals(base.getFrameType()))
      .filter(b -> b.getName().equals(base.getName()))
      .filter(b -> b.getBaseType().equals(base.getBaseType()))
      .findFirst();

    if (dbBase.isPresent()) {
      return dbBase.get();
    }

    // todo: necessary? or move to item base builder
    base.setItems(Set.of());
    base.setId(null);

    var newBase = itemBaseRepository.save(base);
    itemBases.add(newBase);
    return newBase;
  }

}
