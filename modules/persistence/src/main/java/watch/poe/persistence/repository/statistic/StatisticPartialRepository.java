package watch.poe.persistence.repository.statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.statistic.StatisticPartial;
import watch.poe.persistence.model.statistic.StatisticPk;

public interface StatisticPartialRepository extends JpaRepository<StatisticPartial, StatisticPk> {

    void deleteByTypeEquals(String type);

}
