package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryDto;
import watch.poe.app.domain.GroupDto;
import watch.poe.app.service.item.Wrapper;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.repository.CategoryRepository;
import watch.poe.persistence.repository.GroupRepository;

@Slf4j
@Service
public class CategorizationService {

  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  public CategoryDto categoryToCategoryDto(Category category) {
    return CategoryDto.valueOf(category.getName());
  }

  public GroupDto groupToGroupDto(Group group) {
    return GroupDto.valueOf(group.getName());
  }

  public Category getOrSaveCategory(Wrapper wrapper) {
    var itemCategory = wrapper.getBase().getCategory();
    var category = categoryRepository.getByName(itemCategory.getName());

    if (category.isEmpty()) {
      itemCategory = categoryRepository.save(itemCategory);
      log.info("Added category to database: {}", itemCategory);
      return itemCategory;
    }

    return category.get();
  }

  public Group getOrSaveGroup(Wrapper wrapper) {
    var itemGroup = wrapper.getBase().getGroup();
    var group = groupRepository.getByName(itemGroup.getName());

    if (group.isEmpty()) {
      itemGroup = groupRepository.save(itemGroup);
      log.info("Added group to database: {}", itemGroup);
      return itemGroup;
    }

    return group.get();
  }

}
