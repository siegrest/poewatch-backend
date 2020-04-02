package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;

  public List<Account> saveAll(List<String> accounts) {
    // filter out nulls and duplicates
    accounts = accounts.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());

    List<Account> dbAccounts = accountRepository.findAllByNameIn(accounts);

    // find accounts that did not exist in the database
    var newAccountNames = accounts.stream()
      .filter(account -> dbAccounts.stream().noneMatch(dbAccount -> dbAccount.getName().equals(account)))
      .collect(Collectors.toList());
    var newAccounts = newAccountNames.stream()
      .map(account -> {
        return Account.builder()
          .name(account)
          .found(LocalDateTime.now())
          .seen(LocalDateTime.now())
          .build();
      }).collect(Collectors.toList());

    dbAccounts.addAll(accountRepository.saveAll(newAccounts));
    return dbAccounts;
  }

  public Account save(String account) {
    if (account == null || StringUtils.isBlank(account)) {
      return null;
    }

    var dbAccount = accountRepository.findByName(account);
    if (dbAccount != null) {
      return dbAccount;
    }

    var newAccount = Account.builder()
      .name(account)
      .build();

    return accountRepository.save(newAccount);
  }

  public List<Account> saveAllSlow(List<String> accountNames) {
    var accounts = accountNames.stream()
      .map(account -> Account.builder().name(account).build())
      .collect(Collectors.toList());
    return accountRepository.saveAll(accounts);
  }

}
