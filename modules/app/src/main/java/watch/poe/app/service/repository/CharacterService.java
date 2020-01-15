package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.Character;
import watch.poe.persistence.repository.CharacterRepository;

import java.util.Date;

@Slf4j
@Service
public class CharacterService {

  @Autowired
  private CharacterRepository characterRepository;

  @Transactional
  public Character save(Account account, String characterName) {
    if (characterName == null || StringUtils.isBlank(characterName)) {
      return null;
    }

    var character = characterRepository.findByName(characterName);
    if (character == null) {
      character = saveNewCharacter(account, characterName);
    } else {
      character = updateCharacterSeen(character);
    }

    return character;
  }

  private Character saveNewCharacter(Account account, String characterName) {
    var character = Character.builder()
      .account(account)
      .found(new Date())
      .seen(new Date())
      .name(characterName)
      .build();

    return characterRepository.save(character);
  }

  private Character updateCharacterSeen(Character character) {
    character.setSeen(new Date());
    return characterRepository.save(character);
  }

}
