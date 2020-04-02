package watch.poe.stats.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import watch.poe.stats.dto.StatCollector;
import watch.poe.stats.model.code.StatType;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(basePackages = "watch.poe.stats")
@EnableJpaRepositories(basePackages = "watch.poe.stats")
@EntityScan("watch.poe.stats")
public class StatisticsModuleConfig {

  @Bean
  public Set<StatCollector> createCollectors() {
    var collectors = new HashSet<StatCollector>();
    for (var type : StatType.values()) {
      collectors.add(new StatCollector(type));
    }
    return collectors;
  }

}
