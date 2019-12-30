package watch.poe.app.service.stash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import watch.poe.app.domain.StatType;
import watch.poe.app.dto.RiverDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.service.StatisticsService;

@Slf4j
@Service
public class StashParserService {

    @Autowired
    private GsonService gsonService;

    @Autowired
    private StatisticsService statisticsService;

    @Async
    public void process(StringBuilder stashStringBuilder) {
        statisticsService.startTimer(StatType.TIME_REPLY_DESERIALIZE);
        var riverDto = gsonService.toObject(stashStringBuilder.toString(), RiverDto.class);
        statisticsService.clkTimer(StatType.TIME_REPLY_DESERIALIZE);

        statisticsService.startTimer(StatType.TIME_REPLY_PARSE);
        parse(riverDto);
        statisticsService.clkTimer(StatType.TIME_REPLY_PARSE);
    }

    private void parse(RiverDto riverDto) {
        log.info("got {} stashes", riverDto.getStashes().size());
    }
}
