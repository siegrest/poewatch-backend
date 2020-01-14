package watch.poe.app.service.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.resource.VariationDto;
import watch.poe.app.dto.resource.VariationItemDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemVariantService {

  private static final String FILE_LOCATION = "classpath:item_variants.json";
  private static final List<VariationItemDto> items = new ArrayList<>();

  @Autowired
  private GsonService gsonService;

  @EventListener(ApplicationReadyEvent.class)
  public void loadAliases() {
    var json = FileUtility.loadFile(FILE_LOCATION);
    var itemVariations = gsonService.toList(json, VariationItemDto.class);
    items.addAll(itemVariations);
  }

  public Optional<VariationDto> getVariation(ItemDto itemDto) {
    // todo: this
    return Optional.empty();
  }

}
