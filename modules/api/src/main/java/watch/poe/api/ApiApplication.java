package watch.poe.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import watch.poe.persistence.repository.LeagueRepository;

@Slf4j
@EnableCaching
@SpringBootApplication
public class ApiApplication implements CommandLineRunner {

	@Autowired
	LeagueRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("League id 24 -> {}", repository.findById(24));
		log.info("All leagues -> {}", repository.findAll());

//		//Insert
//		var l1 = new League(null, "New 1", null, false, true, false, true, true, new Timestamp(0), new Timestamp(0));
//		log.info("Inserting -> {}", repository.save(l1));

		log.info("All leagues -> {}", repository.findAll());
	}
}
