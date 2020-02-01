package watch.poe.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import watch.poe.persistence.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

  Account findByName(String name);

}
