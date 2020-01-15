package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.domain.Rarity;
import watch.poe.app.dto.river.ItemDto;
import watch.poe.app.service.resource.GroupMappingService;
import watch.poe.app.utility.ItemUtility;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.repository.CategoryRepository;
import watch.poe.persistence.repository.GroupRepository;

@Slf4j
@Service
public class CategorizationService {

  @Autowired
  private GroupMappingService groupMappingService;
  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  public Category parseCategory(ItemDto itemDto) {
    var categoryEnum = determineCategoryDto(itemDto);
    return categoryDtoToCategory(categoryEnum);
  }

  public Group parseGroup(ItemDto itemDto) {
    var categoryEnum = determineCategoryDto(itemDto);
    var groupEnum = determineGroupDto(itemDto, categoryEnum);
    return groupDtoToGroup(groupEnum);
  }

  public CategoryDto determineCategoryDto(ItemDto itemDto) {
    if (itemDto == null) {
      return null;
    }

    var apiCategory = itemDto.getExtended().getCategory();
    if (apiCategory == null) {
      log.error("Missing category for item: {}", itemDto);
      return null;
    }

    if (itemDto.getEnchantMods() != null) {
      return CategoryDto.enchantment;
    }

    // todo: abyssal jewels and belts and flasks are not included
    // todo: maps are included?
    if (ItemUtility.isCraftable(itemDto) && ItemUtility.hasInfluence(itemDto)) {
      return CategoryDto.base;
    }

    if (itemDto.getFrameType() == Rarity.Prophecy) {
      return CategoryDto.prophecy;
    }

    switch (apiCategory) {
      case "currency":
        return CategoryDto.currency;
      case "gems":
        return CategoryDto.gem;
      case "maps":
      case "watchstones":
        return CategoryDto.map;
      case "cards":
        return CategoryDto.card;
      case "flasks":
        return CategoryDto.flask;
      case "jewels":
        return CategoryDto.jewel;
      case "monsters":
        return CategoryDto.beast;
      case "armour":
        return CategoryDto.armour;
      case "accessories":
        return CategoryDto.accessory;
      case "weapons":
        return CategoryDto.weapon;
    }

    // todo: leaguestones have [apiCategory="leaguestones"]
    log.error("Could not determine category for item: {}", itemDto);
    return null;
  }

  public GroupDto determineGroupDto(ItemDto itemDto, CategoryDto category) {
    if (itemDto == null || category == null) {
      return null;
    }

    var apiGroup = ItemUtility.getFirstApiGroup(itemDto);
    var iconCategory = ItemUtility.findIconCategory(itemDto);
    var acceptedGroups = groupMappingService.getGroups(category);

    switch (category) {
      case card:
        return GroupDto.card;
      case flask:
        return GroupDto.flask;
      case jewel:
        return GroupDto.jewel;
      case prophecy:
        return GroupDto.prophecy;
      case accessory:
      case weapon:
      case armour:
      case enchantment:
      case base:

        for (var group : acceptedGroups) {
          if (group.equals(apiGroup)) {
            return GroupDto.valueOf(apiGroup);
          }
        }

        break;
      case currency:

        switch (iconCategory) {
          case "currency":
          case "divination": // stacked deck
            return GroupDto.currency;
          case "essence":
            return GroupDto.essence;
          case "breach":
            return GroupDto.splinter;
          case "oils":
            return GroupDto.oil;
          case "catalysts":
            return GroupDto.catalyst;
          case "influence exalts":
            return GroupDto.influence;
        }

        if (apiGroup != null) {
          switch (apiGroup) {
            case "piece":
              return GroupDto.piece;
            case "resonator":
              return GroupDto.resonator;
            case "fossil":
              return GroupDto.fossil;
            case "incubator":
              return GroupDto.incubator;
          }
        }

        if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Vial of ")) {
          return GroupDto.vial;
        }

        if (itemDto.getTypeLine() != null && itemDto.getTypeLine().startsWith("Timeless")
          && itemDto.getTypeLine().endsWith("Splinter")) {
          return GroupDto.splinter;
        }

        break;
      case gem:

        if ("vaalgems".equals(iconCategory)) {
          return GroupDto.vaal;
        } else if ("activegem".equals(apiGroup)) {
          return GroupDto.skill;
        } else if ("supportgem".equals(apiGroup)) {
          return GroupDto.support;
        }

        break;
      case map:

        if (itemDto.getFrameType() == Rarity.Unique || itemDto.getFrameType() == Rarity.Relic) {
          return GroupDto.unique;
        } else if ("breach".equals(iconCategory)) {
          return GroupDto.fragment;
        } else if ("scarabs".equals(iconCategory)) {
          return GroupDto.scarab;
        } else if (itemDto.getProperties() == null) {
          // mortal fragments
          return GroupDto.fragment;
        } else if ("watchstones".equals(itemDto.getExtended().getCategory())) {
          return GroupDto.watchstone;
        } else if (apiGroup == null) {
          return GroupDto.map;
        }

        break;
      case beast:

        if ("sample".equals(apiGroup)) {
          return GroupDto.sample;
        } else { // todo: find bestiary beast api group
          return GroupDto.beast;
        }

    }

    log.error("Could not determine group for item: {}", itemDto);
    return null;
  }

  public Group groupDtoToGroup(GroupDto groupDto) {
    var group = groupRepository.getByName(groupDto.name());
    if (group.isEmpty()) {
      var newGroup = Group.builder()
        .name(groupDto.name())
        .display(StringUtils.capitalize(groupDto.name()))
        .build();
      log.info("Adding group to database: {}", newGroup);
      return groupRepository.save(newGroup);
//      throw new RuntimeException("Missing group in database for: " + groupDto.name());
    }

    return group.get();
  }

  public Category categoryDtoToCategory(CategoryDto categoryDto) {
    var category = categoryRepository.getByName(categoryDto.name());
    if (category.isEmpty()) {
      var newCategory = Category.builder()
        .name(categoryDto.name())
        .display(StringUtils.capitalize(categoryDto.name()))
        .build();
      log.info("Adding category to database: {}", newCategory);
      return categoryRepository.save(newCategory);
//      throw new RuntimeException("Missing category in database for: " + categoryDto.name());
    }

    return category.get();
  }

  public CategoryDto categoryToCategoryDto(Category category) {
    return CategoryDto.valueOf(category.getName());
  }

  public GroupDto groupToGroupDto(Group group) {
    return GroupDto.valueOf(group.getName());
  }

}
