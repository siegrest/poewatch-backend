package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.StatisticPartial;
import watch.poe.persistence.model.StatisticPk;

public interface StatisticPartialRepository extends JpaRepository<StatisticPartial, StatisticPk> {

    void deleteByTypeEquals(String type);

}
