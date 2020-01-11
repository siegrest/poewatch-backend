package watch.poe.app.utility;

import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.dto.river.PropertyDto;
import watch.poe.app.dto.river.SocketDto;
import watch.poe.app.exception.InvalidIconException;
import watch.poe.app.exception.ItemDiscardException;

public final class ItemUtility {

    public static boolean hasInfluence(ItemDto itemDto) {
        var influences = itemDto.getInfluences();
        return influences != null
          && (influences.getShaper() != null && influences.getShaper()
          || influences.getElder() != null && influences.getElder()
          || influences.getCrusader() != null && influences.getCrusader()
          || influences.getRedeemer() != null && influences.getRedeemer()
          || influences.getHunter() != null && influences.getHunter()
          || influences.getWarlord() != null && influences.getWarlord());
    }

    public static boolean isCraftable(ItemDto itemDto) {
        var frameType = itemDto.getFrameType();
        var corrupted = itemDto.getIsCorrupted() != null && itemDto.getIsCorrupted();

        return !corrupted && (frameType == 0 || frameType == 1 || frameType == 2);
    }

    /**
     * Removes any unnecessary fields from the item's icon
     *
     * @param icon Item's icon
     * @return Formatted icon URL
     */
    public static String formatIcon(String icon) throws InvalidIconException {
        if (icon == null) {
            return null;
        }

        var splitURL = icon.split("\\?", 2);
        var fullIcon = splitURL[0];

        if (splitURL.length > 1) {
            var paramBuilder = new StringBuilder();

            for (String param : splitURL[1].split("&")) {
                var splitParam = param.split("=");

                switch (splitParam[0]) {
                    case "scale": // scale
                    case "w": // width
                    case "h": // height
                    case "mr": // shaped
                    case "mn": // series
                    case "mt": // tier
                    case "mb": // blighted
                    case "relic":
                        paramBuilder.append("&");
                        paramBuilder.append(splitParam[0]);
                        paramBuilder.append("=");
                        paramBuilder.append(splitParam[1]);
                        break;
                    default:
                        var msg = String.format("Unknown item icon query parameter %s in %s", splitParam[0], icon);
                        throw new InvalidIconException(msg);
                }
            }

            // If there are parameters that should be kept, add them to fullIcon
            if (paramBuilder.length() > 0) {
                // Replace the first "&" symbol with "?"
                paramBuilder.setCharAt(0, '?');
                fullIcon += paramBuilder.toString();
            }
        }

        return fullIcon;
    }

    /**
     * Find icon cdn category. For example, if the icon path is
     * "http://web.poecdn.com/image/Art/2DItems/Armours/Helmets/HarbingerShards/Shard1.png"
     * then the icon category is "HarbingerShards"
     *
     * @return Extracted category
     */
    public static String findIconCategory(ItemDto itemDto) {
        var splitItemType = itemDto.getIcon().split("/");
        return splitItemType[splitItemType.length - 2].toLowerCase();
    }

    public static String getFirstApiGroup(ItemDto itemDto) {
        var subCategories = itemDto.getExtended().getSubcategories();

        if (subCategories != null && !subCategories.isEmpty()) {
            return subCategories.get(0).toLowerCase();
        }

        return null;
    }

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
        return itemDto.getName() != null && itemDto.getName().startsWith("Superior ")
          ? itemDto.getName().replace("Superior ", "")
          : itemDto.getName();
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
            throw new ItemDiscardException("Failed to extract map series");
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
            throw new ItemDiscardException("No map series found");
        }
    }

    public static Integer extractLinks(ItemDto itemDto) throws ItemDiscardException {
        if (itemDto.getSockets() == null) {
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

}
