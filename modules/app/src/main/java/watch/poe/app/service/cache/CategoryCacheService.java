package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.CategoryDto;
import watch.poe.persistence.model.Category;
import watch.poe.persistence.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCacheService {

  private final CategoryRepository categoryRepository;
  private final List<Category> categories = new ArrayList<>();

  @EventListener(ApplicationStartedEvent.class)
  public void init() {
    categories.addAll(categoryRepository.findAll());
    if (categories.isEmpty()) {
      initValues();
      log.info("Initialized {} categories", categories.size());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void initValues() {
    for (CategoryDto dto : CategoryDto.values()) {
      var category = Category.builder()
        .name(dto.name())
        // todo: convert to lower case first?
        .display(StringUtils.capitalize(dto.name()))
        .build();

      categoryRepository.save(category);
      categories.add(category);
    }
  }

  public Optional<Category> get(String name) {
    return this.categories.stream()
      .filter(c -> c.getName().equals(name))
      .findFirst();
  }

}
