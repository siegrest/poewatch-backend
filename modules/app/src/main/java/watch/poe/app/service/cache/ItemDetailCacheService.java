package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.item.ItemDetail;
import watch.poe.persistence.repository.item.ItemDetailRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemDetailCacheService {

  private final ItemDetailRepository itemDetailRepository;
  private final List<ItemDetail> itemDetails = new ArrayList<>();

  @PostConstruct
  public void init() {
    itemDetails.addAll(itemDetailRepository.findAll());
  }

  public ItemDetail getOrSave(ItemDetail itemDetail) {
    var dbItem = itemDetails.stream()
      .filter(i -> ItemUtility.itemEquals(i, itemDetail))
      .findFirst();

    if (dbItem.isPresent()) {
      return dbItem.get();
    }

    // todo: necessary? or move to item base builder
    itemDetail.setId(null);
    itemDetail.setFound(LocalDateTime.now());

    var newItem = itemDetailRepository.save(itemDetail);
    itemDetails.add(newItem);
    return newItem;
  }

}
