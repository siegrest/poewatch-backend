package watch.poe.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import watch.poe.app.domain.StatType;
import watch.poe.app.service.statistics.StatisticsService;

import javax.annotation.PreDestroy;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AppApplication implements CommandLineRunner {

    @Autowired
    private StatisticsService statisticsService;

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        statisticsService.addValue(StatType.MISC_APP_STARTUP);
    }

    @PreDestroy
    public void onExit() {
        statisticsService.addValue(StatType.MISC_APP_SHUTDOWN);
    }

}
