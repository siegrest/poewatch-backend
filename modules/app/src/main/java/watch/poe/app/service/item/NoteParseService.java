package watch.poe.app.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.PriceDto;
import watch.poe.app.exception.ItemParseException;
import watch.poe.app.service.resource.CurrencyAliasService;
import watch.poe.persistence.model.code.ParseErrorCode;
import watch.poe.persistence.model.item.FrameType;
import watch.poe.persistence.model.item.ItemDetail;
import watch.poe.persistence.repository.item.ItemBaseRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteParseService {

  private static final List<String> acceptedPrefixes = List.of("~b/o", "~price");

  private final CurrencyAliasService currencyAliasService;
  private final ItemBaseRepository itemBaseRepository;

  public PriceDto parsePrice(String stashNote, String itemNote) {
    var price = parseBuyoutNote(itemNote);
    if (price != null) {
      return price;
    }

    return parseBuyoutNote(stashNote);
  }

  private PriceDto parseBuyoutNote(String note) {
    if (note == null || "".equals(note)) {
      return null;
    }

    String[] noteList = note.split("\\s+");
    if (isNoteListInvalid(noteList)) {
      return null;
    }

    var price = extractValue(noteList);
    if (price == null) {
      return null;
    }

    var currencyName = currencyAliasService.getCurrency(noteList[2]);
    if (currencyName.isEmpty()) {
      return null;
    }

    return PriceDto.builder()
      .price(price)
      .currencyName(currencyName.get().getName())
      .build();
  }

  private boolean isNoteListInvalid(String[] noteList) {
    if (noteList == null || noteList.length < 3) {
      return true;
    }

    return acceptedPrefixes.stream().noneMatch(p -> p.equals(noteList[0]));
  }

  private Double extractValue(String[] noteList) {
    if (isNoteListInvalid(noteList)) {
      return null;
    }

    var valueString = noteList[1];
    double price;

    // items can be listed for "0.5" or "1/2"
    String[] valueList = valueString.split("/");

    if (valueList.length == 1) {
      try {
        price = Double.parseDouble(valueList[0]);
      } catch (NumberFormatException ex) {
        return null;
      }
    } else if (valueList.length == 2) {
      try {
        var val1 = Double.parseDouble(valueList[0]);
        var val2 = Double.parseDouble(valueList[1]);
        price = val1 / val2;
      } catch (NumberFormatException ex) {
        return null;
      }
    } else {
      return null;
    }

    if (Double.isNaN(price)) {
      return null;
    }

    return price;
  }

  public ItemDetail priceToItem(PriceDto priceDto) throws ItemParseException {
    if (priceDto == null) {
      return null;
    }

    var base = itemBaseRepository.findByFrameTypeAndBaseType(FrameType.CURRENCY, priceDto.getCurrencyName());
    if (base.isEmpty()) {
      throw new ItemParseException(ParseErrorCode.MISSING_CURRENCY);
    }

    var items = base.get().getItemDetails();
    if (items.size() != 1) {
      throw new ItemParseException(ParseErrorCode.DUPLICATE_CURRENCY_ITEM);
    }

    return items.stream().findFirst().get();
  }

}
