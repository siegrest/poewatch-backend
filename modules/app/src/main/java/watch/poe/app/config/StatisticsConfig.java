package watch.poe.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import watch.poe.app.domain.StatCollector;
import watch.poe.app.domain.StatGroupType;
import watch.poe.app.domain.StatType;
import watch.poe.app.domain.TimeFrame;

import java.util.List;

@Configuration
public class StatisticsConfig {

  @Bean
  public List<StatCollector> getCollectors() {
    return List.of(
        new StatCollector(StatType.TIME_API_REPLY_DOWNLOAD, StatGroupType.AVG, TimeFrame.M_60),
        new StatCollector(StatType.TIME_API_TTFB, StatGroupType.AVG, TimeFrame.M_60),

        new StatCollector(StatType.TIME_REPLY_PARSE, StatGroupType.AVG, TimeFrame.M_60),
        new StatCollector(StatType.TIME_REPLY_DESERIALIZE, StatGroupType.AVG, TimeFrame.M_60),

        new StatCollector(StatType.COUNT_API_ERRORS_DUPLICATE, StatGroupType.SUM, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_ERRORS_CONNECT_TIMEOUT, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_ERRORS_READ_TIMEOUT, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_ERRORS_CONN_RESET, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_ERRORS_5XX, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_ERRORS_4XX, StatGroupType.COUNT, TimeFrame.M_60),

        new StatCollector(StatType.COUNT_REPLY_SIZE, StatGroupType.AVG, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_API_CALLS, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_TOTAL_STASHES, StatGroupType.SUM, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_TOTAL_ITEMS, StatGroupType.SUM, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_ACCEPTED_ITEMS, StatGroupType.SUM, TimeFrame.M_60),
        new StatCollector(StatType.COUNT_ACTIVE_ACCOUNTS, StatGroupType.SUM, TimeFrame.M_60),

        new StatCollector(StatType.MISC_APP_STARTUP, StatGroupType.COUNT, TimeFrame.M_60),
        new StatCollector(StatType.MISC_APP_SHUTDOWN, StatGroupType.COUNT, TimeFrame.M_60)
    );
  }

}
