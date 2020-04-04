package watch.poe.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.stats.model.StatCollector;
import watch.poe.stats.model.code.StatType;

public interface StatCollectorRepository extends JpaRepository<StatCollector, StatType> {

}
