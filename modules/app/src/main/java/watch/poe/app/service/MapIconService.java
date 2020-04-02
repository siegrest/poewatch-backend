package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.exception.ItemParseException;
import watch.poe.persistence.model.code.DiscardErrorCode;
import watch.poe.persistence.model.code.ParseErrorCode;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapIconService {

  private final GsonService gsonService;

  public Integer parseSeries(ItemDto itemDto) throws ItemParseException {
    if (itemDto == null || StringUtils.isBlank(itemDto.getIcon())) {
      return null;
    }

    // [28,14,{"f":"2DItems\/Maps\/Atlas2Maps\/New\/Museum","w":1,"h":1,"scale":true,"mn":7,"mt":3}]
    Map<String, String> params;
    try {
      var json = extractBase64(itemDto.getIcon());
      params = extractParams(json);
    } catch (Exception ex) {
      throw new ItemParseException(ex, ParseErrorCode.PARSE_MAP_ICON);
    }

    return parseMapSeries(params);
  }

  private String extractBase64(String icon) {
    // https://web.poecdn.com/gen/image/WzI4LDE0LHsiZiI6IjJESXRlbXNcL01hcHNcL0F0bGFzMk1hcHNcL05ld1wvTXVzZXVtIiwidyI6MSwiaCI6MSwic2NhbGUiOnRydWUsIm1uIjo3LCJtdCI6M31d/fb4a9b4077/Item.png
    var tmp = icon.split("/");
    var base64 = tmp[tmp.length - 3];
    var bytes = Base64.getDecoder().decode(base64);
    return new String(bytes);
  }

  private Map<String, String> extractParams(String base64) {
    var paramJson = base64.substring(base64.indexOf('{'), base64.indexOf('}') + 1);
    return gsonService.toObject(paramJson);
  }

  private Integer parseMapSeries(Map<String, String> params) throws ItemParseException {
    /* Currently the series are as such:
     todo: update outdated links
     http://web.poecdn.com/image/Art/2DItems/Maps/Map45.png?scale=1&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map76.png?scale=1&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/AtlasMaps/Chimera.png?scale=1&scaleIndex=0&w=1&h=1
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=1&mt=0
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=2&mt=0
     http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=3&mt=0
    */

    var iconCategory = parseIconCategory(params);
    var seriesNumber = parseSeriesParam(params);

    if (iconCategory.equalsIgnoreCase("Maps")) {
      return 0;
    } else if (iconCategory.equalsIgnoreCase("act4maps")) {
      return 1;
    } else if (iconCategory.equalsIgnoreCase("AtlasMaps")) {
      return 2;
    } else if (iconCategory.equalsIgnoreCase("New") && seriesNumber.isPresent()) {
      return seriesNumber.get() + 2;
    } else {
      throw new ItemParseException(DiscardErrorCode.INVALID_MAP_SERIES);
    }
  }

  private String parseIconCategory(Map<String, String> paramMap) throws ItemParseException {
    if (paramMap == null || !paramMap.containsKey("f")) {
      throw new ItemParseException(ParseErrorCode.PARSE_MAP_ICON_PARAM_F);
    }

    var split = paramMap.get("f").split("/");
    return split[split.length - 2].toLowerCase();
  }

  private Optional<Integer> parseSeriesParam(Map<String, String> paramMap) throws ItemParseException {
    // older maps will not have this param
    if (!paramMap.containsKey("mn")) {
      return Optional.empty();
    }

    try {
      var series = Integer.parseInt(paramMap.get("mn"));
      return Optional.of(series);
    } catch (Exception ex) {
      throw new ItemParseException(ex, ParseErrorCode.PARSE_MAP_ICON_PARAM_MN);
    }
  }

}
