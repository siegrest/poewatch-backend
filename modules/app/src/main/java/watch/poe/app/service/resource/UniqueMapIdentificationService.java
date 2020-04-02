package watch.poe.app.service.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.UniqueMapDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniqueMapIdentificationService {

  private static final String FILE_LOCATION = "classpath:unique_maps.json";
  private static final List<UniqueMapDto> UNIQUE_MAP_DTOS = new ArrayList<>();

  private final GsonService gsonService;

  @PostConstruct
  public void loadAliases() {
    var json = FileUtility.loadFile(FILE_LOCATION);
    var maps = gsonService.toList(json, UniqueMapDto.class);
    UNIQUE_MAP_DTOS.addAll(maps);
  }

  public Optional<UniqueMapDto> identifyMap(ItemDto itemDto) {
    return UNIQUE_MAP_DTOS.stream()
      .filter(i -> i.getType().equals(itemDto.getTypeLine()))
      .filter(i -> i.getRarity().is(itemDto.getFrameType()))
      .findFirst();
  }

}
