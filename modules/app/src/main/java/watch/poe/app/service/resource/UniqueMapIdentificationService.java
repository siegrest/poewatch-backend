package watch.poe.app.service.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.UniqueMap;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UniqueMapIdentificationService {

  private static final String FILE_LOCATION = "classpath:unique_maps.json";
  private static final List<UniqueMap> uniqueMaps = new ArrayList<>();

  @Autowired
  private GsonService gsonService;

  @EventListener(ApplicationReadyEvent.class)
  public void loadAliases() {
    var json = FileUtility.loadFile(FILE_LOCATION);
    var maps = gsonService.toList(json, UniqueMap.class);
    uniqueMaps.addAll(maps);
  }

  public Optional<UniqueMap> identifyMap(ItemDto itemDto) {
    return uniqueMaps.stream()
      .filter(i -> i.getType().equals(itemDto.getTypeLine()))
      .filter(i -> i.getRarity() == itemDto.getFrameType())
      .findFirst();
  }

}
