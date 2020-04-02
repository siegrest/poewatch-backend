package watch.poe.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import watch.poe.stats.model.code.StatType;
import watch.poe.stats.service.StatisticsService;

import javax.annotation.PreDestroy;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class AppApplication {

  private final StatisticsService statisticsService;

  public static void main(String[] args) {
    SpringApplication.run(AppApplication.class, args);
  }

  @EventListener(ApplicationStartedEvent.class)
  public void run() {
    statisticsService.addValue(StatType.MISC_APP_STARTUP);
  }

  @PreDestroy
  public void onExit() {
    statisticsService.addValue(StatType.MISC_APP_SHUTDOWN);
  }

}
