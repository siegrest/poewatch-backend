package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.CategoryEnum;
import watch.poe.app.dto.resource.GroupMappingDto;
import watch.poe.app.utility.FileUtility;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GroupMappingService {

    private static final String FILE_LOCATION = "classpath:group_mappings.json";
    private static final List<GroupMappingDto> categories = new ArrayList<>();

    @Autowired
    private GsonService gsonService;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        var json = FileUtility.loadFile(FILE_LOCATION);
        var dtoList = gsonService.toList(json, GroupMappingDto.class);
        categories.addAll(dtoList);
    }

    public boolean isValidGroup(String group) {
        return getCategory(group) != null;
    }

    public String getCategory(String group) {
        return categories.stream()
                .filter(c -> c.getGroups().stream().anyMatch(g -> g.equals(group)))
                .findFirst()
                .map(GroupMappingDto::getCategory)
                .orElse(null);
    }

    public List<String> getGroups(CategoryEnum categoryEnum) {
        return categories.stream()
                .filter(c -> c.getCategory().equals(categoryEnum.name()))
                .findFirst()
                .map(GroupMappingDto::getGroups)
                .orElse(null);
    }
}
