package watch.poe.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.stats.model.StatisticPartial;
import watch.poe.stats.model.StatisticPk;

public interface StatisticPartialRepository extends JpaRepository<StatisticPartial, StatisticPk> {

  void deleteByTypeEquals(String type);

}
