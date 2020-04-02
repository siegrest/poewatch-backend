package watch.poe.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import watch.poe.persistence.config.PersistenceModuleConfig;
import watch.poe.stats.config.StatisticsModuleConfig;

@Configuration
@Import({PersistenceModuleConfig.class, StatisticsModuleConfig.class})
@PropertySource({"classpath:poe-watch.properties"})
@RequiredArgsConstructor
public class AppModuleConfig {

    private final Environment env;

    public String getProperty(String pPropertyKey) {
        return env.getProperty(pPropertyKey);
    }

    public Integer getPropertyAsInt(String pPropertyKey) {
        return Integer.parseInt(getProperty(pPropertyKey));
    }

}
