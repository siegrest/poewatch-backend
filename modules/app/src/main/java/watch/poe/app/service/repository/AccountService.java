package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.repository.AccountRepository;

import java.util.Date;

@Slf4j
@Service
public class AccountService {

  @Autowired
  private AccountRepository accountRepository;

  @Transactional
  public Account save(String accountName) {
    if (accountName == null || StringUtils.isBlank(accountName)) {
      return null;
    }

    var account = accountRepository.findByName(accountName);
    if (account == null) {
      account = saveNewAccount(accountName);
    } else {
      account = updateAccountSeen(account);
    }

    return account;
  }

  private Account saveNewAccount(String accountName) {
    var account = Account.builder()
      .found(new Date())
      .seen(new Date())
      .name(accountName)
      .build();

    return accountRepository.save(account);
  }

  private Account updateAccountSeen(Account account) {
    account.setSeen(new Date());
    return accountRepository.save(account);
  }

}
