package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.persistence.model.Item;
import watch.poe.persistence.repository.ItemBaseRepository;

@Slf4j
@Service
public class ItemParseService {

  @Autowired
  private CategorizationService categorizationService;
  @Autowired
  private ItemBaseRepository itemBaseRepository;

  public Item parse(ItemDto itemDto) {
    var category = categorizationService.determineCategory(itemDto);
    var group = categorizationService.determineGroup(itemDto);

    // todo: me

    return Item.builder().build();
  }

}
