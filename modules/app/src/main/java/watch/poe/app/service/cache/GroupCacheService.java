package watch.poe.app.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.domain.GroupDto;
import watch.poe.persistence.model.Group;
import watch.poe.persistence.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupCacheService {

  private final GroupRepository groupRepository;
  private final List<Group> groups = new ArrayList<>();

  @EventListener(ApplicationStartedEvent.class)
  public void init() {
    groups.addAll(groupRepository.findAll());
    if (groups.isEmpty()) {
      initValues();
      log.info("Initialized {} groups", groups.size());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void initValues() {
    for (GroupDto dto : GroupDto.values()) {
      var group = Group.builder()
        .name(dto.name())
        // todo: convert to lower case first?
        .display(StringUtils.capitalize(dto.name()))
        .build();

      groupRepository.save(group);
      groups.add(group);
    }
  }

  public Optional<Group> get(String name) {
    return this.groups.stream()
      .filter(g -> g.getName().equals(name))
      .findFirst();
  }

}
