package watch.poe.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.RiverItemDto;

@Slf4j
@Service
public class ItemParseService {

    @Autowired
    private CategorizationService categorizationService;

    public boolean parse(RiverItemDto itemDto) {
        var category = categorizationService.determineCategory(itemDto);
        var group = categorizationService.determineGroup(itemDto, category);

        return category != null && group != null;
    }

}
