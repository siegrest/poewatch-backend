package watch.poe.app.service.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CorruptedItemService {

  // https://pathofexile.gamepedia.com/Corrupted

  private static final String FILE_LOCATION = "classpath:corrupted_items.json";
  private static final List<String> corrupted_uniques = new ArrayList<>();

  @Autowired
  private GsonService gsonService;

  @EventListener(ApplicationReadyEvent.class)
  public void loadAliases() {
    var json = FileUtility.loadFile(FILE_LOCATION);
    var itemVariations = gsonService.toList(json, String.class);
    corrupted_uniques.addAll(itemVariations);
  }

  public boolean isCorrupted(String name) {
    return corrupted_uniques.contains(name);
  }

}
