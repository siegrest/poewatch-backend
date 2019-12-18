package watch.poe.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import watch.poe.persistence.config.PersistenceModuleConfig;

@Configuration
@Import(PersistenceModuleConfig.class)
public class ApiModuleConfig {
}
