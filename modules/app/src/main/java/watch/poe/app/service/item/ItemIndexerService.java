package watch.poe.app.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.statistics.StatType;
import watch.poe.app.domain.wrapper.RiverWrapper;
import watch.poe.app.service.StatisticsService;
import watch.poe.app.service.repository.LeagueItemEntryService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.*;
import watch.poe.persistence.repository.CategoryRepository;
import watch.poe.persistence.repository.GroupRepository;
import watch.poe.persistence.repository.ItemBaseRepository;
import watch.poe.persistence.repository.ItemRepository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemIndexerService {

  private final ItemRepository itemRepository;
  private final ItemBaseRepository itemBaseRepository;
  private final GroupRepository groupRepository;
  private final CategoryRepository categoryRepository;
  private final StatisticsService statisticsService;
  // todo: this should not be here
  private final LeagueItemEntryService itemEntryService;

  @Async
  public Future<Boolean> startIndexJob(RiverWrapper wrapper) {
    statisticsService.startTimer(StatType.TIME_REPLY_INDEX);

    for (LeagueItemEntry entry : wrapper.getEntries()) {
      var item = index(entry.getItem());
      entry.setItem(item);
      // todo: this should not be here
      itemEntryService.save(entry);
    }

    statisticsService.clkTimer(StatType.TIME_REPLY_INDEX, true);

    return CompletableFuture.completedFuture(true);
  }

  public Item index(Item item) {
    var base = item.getBase();

    var category = getOrSaveCategory(base.getCategory());
    base.setCategory(category);

    var group = getOrSaveGroup(base.getGroup());
    base.setGroup(group);

    var itemBase = getOrSaveBase(base);
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

  public ItemBase getOrSaveBase(ItemBase base) {
    var dbBase = itemBaseRepository.findByCategoryAndGroupAndFrameTypeAndNameAndBaseType(base.getCategory(),
      base.getGroup(), base.getFrameType(), base.getName(), base.getBaseType());

    if (dbBase.isEmpty()) {
      base.setItems(Set.of());
      base = itemBaseRepository.save(base);
      log.debug("Saved new item base {}", base);
      return base;
    }

    return dbBase.get();
  }

  public Item saveNewItem(Item item) {
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

  public Category getOrSaveCategory(Category category) {
    var dbCategory = categoryRepository.getByName(category.getName());

    if (dbCategory.isEmpty()) {
      category.setDisplay(StringUtils.capitalize(category.getName()));
      category = categoryRepository.save(category);
      log.info("Added category to database: {}", category);
      return category;
    }

    return dbCategory.get();
  }

  public Group getOrSaveGroup(Group group) {
    var dbGroup = groupRepository.getByName(group.getName());

    if (dbGroup.isEmpty()) {
      group.setDisplay(StringUtils.capitalize(group.getName()));
      group = groupRepository.save(group);
      log.info("Added group to database: {}", group);
      return group;
    }

    return dbGroup.get();
  }

}
