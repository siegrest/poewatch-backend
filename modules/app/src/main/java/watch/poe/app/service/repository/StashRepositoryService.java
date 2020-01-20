package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.river.StashDto;
import watch.poe.persistence.model.Account;
import watch.poe.persistence.model.League;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.repository.StashRepository;

import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class StashRepositoryService {

  @Autowired
  public StashRepository stashRepository;

  public Stash save(League league, Account account, StashDto stashDto) {
    if (stashDto.getId() == null || StringUtils.isBlank(stashDto.getId())) {
      return null;
    }

    var stash = stashRepository.findById(stashDto.getId());
    if (stash.isEmpty()) {
      if (league == null || account == null || stashDto.getItems() == null || stashDto.getItems().isEmpty()) {
        return null;
      }

      return saveNewStash(league, account, stashDto);
    } else {
      if (league == null || account == null || stashDto.getItems() == null || stashDto.getItems().isEmpty()) {
        stashRepository.deleteById(stashDto.getId());
        return null;
      }

      return updateStash(stash.get(), stashDto);
    }
  }

  private Stash saveNewStash(League league, Account account, StashDto stashDto) {
    var stash = Stash.builder()
      .account(account)
      .league(league)
      .updates(0)
      .found(new Date())
      .seen(new Date())
      .itemCount(stashDto.getItems().size())
      .id(stashDto.getId())
      .build();

    return stashRepository.save(stash);
  }

  private Stash updateStash(Stash stash, StashDto stashDto) {
    stash.setUpdates(stash.getUpdates() + 1);
    stash.setSeen(new Date());
    stash.setItemCount(stashDto.getItems().size());

    if (stashDto.getItems() == null || stashDto.getItems().isEmpty()) {
      stash.setItems(Set.of());
    }

    return stashRepository.save(stash);
  }

}
