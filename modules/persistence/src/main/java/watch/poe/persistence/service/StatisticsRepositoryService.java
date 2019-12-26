package watch.poe.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.StatisticHistory;
import watch.poe.persistence.model.StatisticPartial;
import watch.poe.persistence.repository.StatisticHistoryRepository;
import watch.poe.persistence.repository.StatisticPartialRepository;

import java.util.Date;
import java.util.List;

@Service
public class StatisticsRepositoryService {

    @Autowired
    private StatisticHistoryRepository statisticHistoryRepository;
    @Autowired
    private StatisticPartialRepository statisticPartialRepository;

    public List<StatisticPartial> getPartialStatistics() {
        return statisticPartialRepository.findAll();
    }

    public void deletePartialByType(String type) {
        statisticPartialRepository.deleteByTypeEquals(type);
    }

    public void saveToHistory(String type, Date time, Long value) {
        var s = StatisticHistory.builder()
                .time(time)
                .type(type)
                .value(value)
                .build();

        statisticHistoryRepository.save(s);
    }

    public void saveToPartial(String type, Date time, Long sum, Long count) {
        var partialBuilder = StatisticPartial.builder()
                .time(time)
                .type(type)
                .count(count)
                .sum(sum);

        statisticPartialRepository.save(partialBuilder.build());
    }

}
