package watch.poe.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.stats.model.StatHistory;

public interface StatHistoryRepository extends JpaRepository<StatHistory, Long> {
}
