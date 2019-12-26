package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.StatisticHistory;
import watch.poe.persistence.model.StatisticPk;

public interface StatisticHistoryRepository extends JpaRepository<StatisticHistory, StatisticPk> {
}
