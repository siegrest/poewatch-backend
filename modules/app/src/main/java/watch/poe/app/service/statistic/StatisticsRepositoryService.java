package watch.poe.app.service.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.statistic.StatisticHistory;
import watch.poe.persistence.model.statistic.StatisticPartial;
import watch.poe.persistence.repository.statistic.StatisticHistoryRepository;
import watch.poe.persistence.repository.statistic.StatisticPartialRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsRepositoryService {

  private final StatisticHistoryRepository statisticHistoryRepository;
  private final StatisticPartialRepository statisticPartialRepository;

  public List<StatisticPartial> getPartialStatistics() {
    return statisticPartialRepository.findAll();
  }

  public void deletePartialByType(String type) {
    statisticPartialRepository.deleteByTypeEquals(type);
  }

  public void saveToHistory(String type, LocalDateTime time, Long value) {
        var s = StatisticHistory.builder()
                .time(time)
                .type(type)
                .value(value)
                .build();

        statisticHistoryRepository.save(s);
    }

    public void saveToPartial(String type, LocalDateTime time, Long sum, Long count) {
        var partialBuilder = StatisticPartial.builder()
                .time(time)
                .type(type)
                .count(count)
                .sum(sum);

        statisticPartialRepository.save(partialBuilder.build());
    }

}
