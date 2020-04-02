package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.Character;
import watch.poe.persistence.repository.CharacterRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

  private final CharacterRepository characterRepository;

  @Transactional
  public List<Character> saveAll(List<Character> characters) {
    var names = characters.stream()
      .map(Character::getName)
      .distinct()
      .collect(Collectors.toList());

    var dbCharacters = characterRepository.findAllByNameIn(names);

    var newCharacters = characters.stream()
      .filter(character -> {
        return dbCharacters.stream().noneMatch(dbChar -> dbChar.getName().equals(character.getName()));
      }).collect(Collectors.toList());

    dbCharacters.addAll(characterRepository.saveAll(newCharacters));
    return dbCharacters;
  }

  public Character save(Account account, String character) {
    if (account.getId() == null || character == null || StringUtils.isBlank(character)) {
      return null;
    }

    var dbCharacter = characterRepository.findByName(character);
    if (dbCharacter.isPresent()) {
      return characterRepository.save(dbCharacter.get());
    }

    var newCharacter = Character.builder()
      .name(character)
      .account(account)
      .build();

    return characterRepository.save(newCharacter);
  }

}
