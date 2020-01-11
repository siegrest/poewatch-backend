package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.PropertyDto;
import watch.poe.app.dto.river.SocketDto;
import watch.poe.app.exception.ItemDiscardException;
import watch.poe.app.utility.ItemUtility;

@Slf4j
@Service
public class ItemParseService {

    @Autowired
    private CategorizationService categorizationService;

    public static Integer extractMapTier(ItemDto itemDto) throws ItemDiscardException {
        if (itemDto.getProperties() != null) {
            for (PropertyDto prop : itemDto.getProperties()) {
                if (!"Map Tier".equals(prop.getName()) || prop.getValues().isEmpty()) {
                    continue;
                } else if (prop.getValues().get(0).isEmpty()) {
                    break;
                }

                return Integer.parseInt(prop.getValues().get(0).get(0));
            }
        }

        throw new ItemDiscardException("No map tier found");
    }

    public static String replaceMapSuperiorPrefix(ItemDto itemDto) {
        // todo: is it name or typeline
        var name = itemDto.getName();
        if (itemDto.getName().startsWith("Superior ") || itemDto.getTypeLine().startsWith("Superior ")) {
            log.info("[A1] Found item: {}", itemDto);
        }
        return name != null && name.startsWith("Superior ") ? name.replace("Superior ", "") : name;
    }

    public static Integer extractMapSeries(ItemDto itemDto) throws ItemDiscardException {
        /* Currently the series are as such:
         http://web.poecdn.com/image/Art/2DItems/Maps/Map45.png?scale=1&w=1&h=1
         http://web.poecdn.com/image/Art/2DItems/Maps/act4maps/Map76.png?scale=1&w=1&h=1
         http://web.poecdn.com/image/Art/2DItems/Maps/AtlasMaps/Chimera.png?scale=1&scaleIndex=0&w=1&h=1
         http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=1&mt=0
         http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=2&mt=0
         http://web.poecdn.com/image/Art/2DItems/Maps/Atlas2Maps/New/VaalTempleBase.png?scale=1&w=1&h=1&mn=3&mt=0
        */

        var iconCategory = ItemUtility.findIconCategory(itemDto);
        var seriesNumber = 0;

        // Attempt to find series number for newer maps
        try {
            var iconParams = itemDto.getIcon().split("\\?", 2)[1].split("&");
            for (var param : iconParams) {
                var splitParam = param.split("=");
                if (splitParam[0].equals("mn")) {
                    seriesNumber = Integer.parseInt(splitParam[1]);
                    break;
                }
            }
        } catch (Exception ex) {
            // If it failed, it failed. Doesn't really matter.
            log.error("Failed to extract series from map: {}", itemDto);
        }

        if (iconCategory.equalsIgnoreCase("Maps")) {
            return 0;
        } else if (iconCategory.equalsIgnoreCase("act4maps")) {
            return 1;
        } else if (iconCategory.equalsIgnoreCase("AtlasMaps")) {
            return 2;
        } else if (iconCategory.equalsIgnoreCase("New") && seriesNumber > 0) {
            return seriesNumber + 2;
        } else {
            log.error("Couldn't determine series of map: {}", itemDto);
            throw new ItemDiscardException("No map series found");
        }
    }

    public static Integer extractLinks(ItemDto itemDto) throws ItemDiscardException {
        if (itemDto.getSockets() == null) {
            log.error("Couldn't find item sockets for: {}", itemDto);
            throw new ItemDiscardException("No sockets found");
        }

        // Group links together
        Integer[] linkArray = new Integer[]{0, 0, 0, 0, 0, 0};
        for (SocketDto socket : itemDto.getSockets()) {
            linkArray[socket.getGroup()]++;
        }

        // Find largest single link
        int largestLink = 0;
        for (Integer link : linkArray) {
            if (link > largestLink) {
                largestLink = link;
            }
        }

        return largestLink > 4 ? largestLink : null;
    }

    public static Integer extractStackSize(ItemDto itemDto) throws ItemDiscardException {
        if (itemDto.getStackSize() == null || itemDto.getProperties() == null) {
            return null;
        }

        /*
        This is what it looks like as JSON:
            "properties": [{
                "name": "Stack Size",
                "values": [["42/1000", 0]],
                "displayMode": 0
              }
            ]
         */

        // Find first stacks size property
        var oProperty = itemDto.getProperties().stream()
                .filter(i -> "Stack Size".equals(i.getName()))
                .findFirst();
        if (oProperty.isEmpty()) {
            return null;
        }

        var property = oProperty.get();
        if (property.getValues().isEmpty() || property.getValues().get(0).isEmpty()) {
            throw new ItemDiscardException("Couldn't locate stack size");
        }

        var stackSizeString = property.getValues().get(0).get(0);
        var index = stackSizeString.indexOf("/");

        // Must contain the slash eg "42/1000"
        if (index < 0) {
            throw new ItemDiscardException("Couldn't locate stack size slash");
        }

        try {
            return Integer.parseInt(stackSizeString.substring(index + 1));
        } catch (NumberFormatException ex) {
            throw new ItemDiscardException("Couldn't parse stack size");
        }
    }

    public boolean parse(ItemDto itemDto) {
        var category = categorizationService.determineCategory(itemDto);
        var group = categorizationService.determineGroup(itemDto, category);

        return category != null && group != null;
    }

}
