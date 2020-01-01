package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.RiverStashDto;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.Character;
import watch.poe.persistence.repository.AccountRepository;
import watch.poe.persistence.repository.CharacterRepository;

import java.util.Date;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CharacterRepository characterRepository;

    public Account save(RiverStashDto riverStashDto) {
        var characterName = riverStashDto.getLastCharacterName();
        var accountName = riverStashDto.getAccountName();

        var account = accountRepository.findByName(accountName);
        if (account == null) {
            account = saveNewAccount(accountName, characterName);
            return account;
        }

        account = updateAccountSeen(account);

        var character = characterRepository.findByName(characterName);
        if (character == null) {
            saveNewCharacter(account, characterName);
            return account;
        }

        updateCharacterSeen(character);
        return account;
    }

    private Account saveNewAccount(String accountName, String characterName) {
        var account = Account.builder()
                .found(new Date())
                .seen(new Date())
                .name(accountName)
                .build();

        account = accountRepository.save(account);
        saveNewCharacter(account, characterName);
        return accountRepository.getOne(account.getId());
    }

    private Character saveNewCharacter(Account account, String characterName) {
        var character = Character.builder()
                .account(account)
                .found(new Date())
                .seen(new Date())
                .name(characterName)
                .build();

        characterRepository.save(character);
        return characterRepository.getOne(character.getId());
    }

    private Account updateAccountSeen(Account account) {
        account.setSeen(new Date());
        return accountRepository.save(account);
    }

    private Character updateCharacterSeen(Character character) {
        character.setSeen(new Date());
        return characterRepository.save(character);
    }

}
