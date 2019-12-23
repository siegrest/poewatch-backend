package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.RiverDto;
import watch.poe.app.service.GsonService;

@Slf4j
@Service
public class StashParserService {

    @Autowired
    private GsonService gsonService;

    @Async
    public void parse(StringBuilder stashJsonString) {

        var riverDto = gsonService.toObject(stashJsonString.toString(), RiverDto.class);
        log.info("got {} stashes", riverDto.getStashes().size());

    }

}
