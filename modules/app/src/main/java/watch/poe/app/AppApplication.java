package watch.poe.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AppApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    }

}
