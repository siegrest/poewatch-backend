package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.persistence.model.Item;

@Slf4j
@Service
public class ItemParseService {

  public Item parse(ItemDto itemDto) {


    return Item.builder().build();
  }

}
