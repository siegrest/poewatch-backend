package watch.poe.app.service.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.resource.VariationDto;
import watch.poe.app.dto.resource.VariationItemDto;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;
import watch.poe.persistence.domain.FrameType;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemVariantService {

  private static final String FILE_LOCATION = "classpath:item_variants.json";
  private static final List<VariationItemDto> variations = new ArrayList<>();

  private final GsonService gsonService;

  @PostConstruct
  public void loadAliases() {
    var json = FileUtility.loadFile(FILE_LOCATION);
    var itemVariations = gsonService.toList(json, VariationItemDto.class);
    variations.addAll(itemVariations);
  }

  public Optional<VariationDto> getVariation(ItemDto itemDto) {
    var variationItem = variations.stream()
      .filter(v -> v.getName().equals(itemDto.getName()))
      .findFirst();

    if (variationItem.isEmpty()) {
      return Optional.empty();
    }

    for (var variation : variationItem.get().getVariations()) {
      // Edge case for prophecies
      if (FrameType.PROPHECY.is(itemDto.getFrameType())) {
        var firstMod = variation.getMods().stream().findFirst().orElseThrow();
        if (itemDto.getProphecyText().contains(firstMod)) {
          return Optional.of(variation);
        } else {
          continue;
        }
      }

      var matches = 0;

      for (var variantMod : variation.getMods()) {
        for (String itemMod : itemDto.getExplicitMods()) {
          if (itemMod.contains(variantMod)) {
            matches++;
            break;
          }
        }
      }

      if (matches == variation.getMods().size()) {
        return Optional.of(variation);
      }
    }

    return Optional.empty();
  }

  public boolean hasVariation(ItemDto itemDto) {
    return variations.stream()
      .filter(v -> v.getName().equals(itemDto.getName()))
      .findFirst()
      .isEmpty();
  }

}
