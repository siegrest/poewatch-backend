package watch.poe.persistence.repository.statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.statistic.StatisticHistory;
import watch.poe.persistence.model.statistic.StatisticPk;

public interface StatisticHistoryRepository extends JpaRepository<StatisticHistory, StatisticPk> {
}
