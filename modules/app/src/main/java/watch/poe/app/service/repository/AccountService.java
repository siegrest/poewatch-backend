package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;

  public Account save(String account) {
    if (account == null || StringUtils.isBlank(account)) {
      return null;
    }

    var dbAccount = accountRepository.findByName(account);
    if (dbAccount != null) {
      return dbAccount;
//      return accountRepository.save(dbAccount);
    }

    var newAccount = Account.builder()
      .name(account)
      .build();

    return accountRepository.save(newAccount);
  }

  public List<Account> saveAll(List<String> accountNames) {
    var accounts = accountNames.stream()
      .map(account -> Account.builder().name(account).build())
      .collect(Collectors.toList());
    return accountRepository.saveAll(accounts);
  }

}
