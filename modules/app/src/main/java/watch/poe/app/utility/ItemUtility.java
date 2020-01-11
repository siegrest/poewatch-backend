package watch.poe.app.utility;

import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.exception.InvalidIconException;

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

}
