package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryEnum;
import watch.poe.app.domain.GroupEnum;
import watch.poe.app.dto.RiverItemDto;
import watch.poe.app.utility.ItemUtility;

@Slf4j
@Service
public class CategorizationService {

    @Autowired
    private GroupMappingService groupMappingService;

    public CategoryEnum determineCategory(RiverItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        var apiCategory = itemDto.getExtended().getCategory();
        if (apiCategory == null) {
            log.error("Missing category for item: {}", itemDto);
            return null;
        }

        if (itemDto.getEnchantMods() != null) {
            return CategoryEnum.enchantment;
        }

        // todo: abyssal jewels and belts and flasks are not included
        // todo: maps are included?
        if (ItemUtility.isCraftable(itemDto) && ItemUtility.hasInfluence(itemDto)) {
            return CategoryEnum.base;
        }

        if (itemDto.getFrameType() == 8) {
            return CategoryEnum.prophecy;
        }

        switch (apiCategory) {
            case "currency":
                return CategoryEnum.currency;
            case "gems":
                return CategoryEnum.gem;
            case "maps":
            case "watchstones":
                return CategoryEnum.map;
            case "cards":
                return CategoryEnum.card;
            case "flasks":
                return CategoryEnum.flask;
            case "jewels":
                return CategoryEnum.jewel;
            case "monsters":
                return CategoryEnum.beast;
            case "armour":
                return CategoryEnum.armour;
            case "accessories":
                return CategoryEnum.accessory;
            case "weapons":
                return CategoryEnum.weapon;
        }

        // todo: leaguestones have [apiCategory="leaguestones"]
        log.error("Could not determine category for item: {}", itemDto);
        return null;
    }

    public GroupEnum determineGroup(RiverItemDto itemDto, CategoryEnum category) {
        if (itemDto == null || category == null) {
            return null;
        }

        var apiGroup = ItemUtility.getFirstApiGroup(itemDto);
        var iconCategory = ItemUtility.findIconCategory(itemDto);
        var acceptedGroups = groupMappingService.getGroups(category);

        switch (category) {
            case card:
                return GroupEnum.card;
            case flask:
                return GroupEnum.flask;
            case jewel:
                return GroupEnum.jewel;
            case prophecy:
                return GroupEnum.prophecy;
            case accessory:
            case weapon:
            case armour:
            case enchantment:
            case base:

                for (var group : acceptedGroups) {
                    if (group.equals(apiGroup)) {
                        return GroupEnum.valueOf(apiGroup);
                    }
                }

                break;
            case currency:

                switch (iconCategory) {
                    case "currency":
                    case "divination": // stacked deck
                        return GroupEnum.currency;
                    case "essence":
                        return GroupEnum.essence;
                    case "breach":
                        return GroupEnum.splinter;
                    case "oils":
                        return GroupEnum.oil;
                    case "catalysts":
                        return GroupEnum.catalyst;
                    case "influence exalts":
                        return GroupEnum.influence;
                }

                if (apiGroup != null) {
                    switch (apiGroup) {
                        case "piece":
                            return GroupEnum.piece;
                        case "resonator":
                            return GroupEnum.resonator;
                        case "fossil":
                            return GroupEnum.fossil;
                        case "incubator":
                            return GroupEnum.incubator;
                    }
                }

                if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Vial of ")) {
                    return GroupEnum.vial;
                }

                if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Timeless")
                        && itemDto.getTypeLine().endsWith("Splinter")) {
                    return GroupEnum.splinter;
                }

                break;
            case gem:

                if ("vaalgems".equals(iconCategory)) {
                    return GroupEnum.vaal;
                } else if ("activegem".equals(apiGroup)) {
                    return GroupEnum.skill;
                } else if ("supportgem".equals(apiGroup)) {
                    return GroupEnum.support;
                }

                break;
            case map:

                if (itemDto.getFrameType() == 3 || itemDto.getFrameType() == 9) {
                    return GroupEnum.unique;
                } else if ("breach".equals(iconCategory)) {
                    return GroupEnum.fragment;
                } else if ("scarabs".equals(iconCategory)) {
                    return GroupEnum.scarab;
                } else if (itemDto.getProperties() == null) {
                    // todo: remove log and add comment which edge case this handles
                    log.info("[A0] Found item: {}", itemDto);
                    return GroupEnum.fragment;
                } else if ("watchstones".equals(itemDto.getExtended().getCategory())) {
                    return GroupEnum.watchstone;
                } else if (apiGroup == null) {
                    return GroupEnum.map;
                }

                break;
            case beast:

                if ("sample".equals(apiGroup)) {
                    return GroupEnum.sample;
                } else { // todo: find bestiary beast api group
                    return GroupEnum.beast;
                }

        }

        log.error("Could not determine group for item: {}", itemDto);
        return null;
    }

}
