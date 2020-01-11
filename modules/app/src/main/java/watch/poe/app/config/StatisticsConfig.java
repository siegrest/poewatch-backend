package watch.poe.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import watch.poe.app.service.statistics.StatCollector;
import watch.poe.app.service.statistics.StatType;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class StatisticsConfig {

  @Bean
  public Set<StatCollector> createCollectors() {
    var collectors = new HashSet<StatCollector>();
    for (var type : StatType.values()) {
      collectors.add(new StatCollector(type));
    }
    return collectors;
  }

}