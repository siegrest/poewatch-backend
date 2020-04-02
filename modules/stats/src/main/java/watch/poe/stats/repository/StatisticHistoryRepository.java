package watch.poe.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.stats.model.StatisticHistory;
import watch.poe.stats.model.StatisticPk;

public interface StatisticHistoryRepository extends JpaRepository<StatisticHistory, StatisticPk> {

}
