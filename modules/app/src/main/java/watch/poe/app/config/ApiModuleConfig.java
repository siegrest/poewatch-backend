package watch.poe.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import watch.poe.persistence.config.PersistenceModuleConfig;

@Configuration
@Import(PersistenceModuleConfig.class)
@PropertySource({"classpath:${envTarget:development}.properties"})
public class ApiModuleConfig {

    @Autowired
    private Environment env;

    public String getProperty(String pPropertyKey) {
        return env.getProperty(pPropertyKey);
    }
}
