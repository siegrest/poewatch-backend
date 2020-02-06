package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.repository.StashRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StashService {

  public final StashRepository stashRepository;

  public List<Stash> saveAll(List<Stash> stashes) {
    return stashRepository.saveAll(stashes);
  }

  public void markStale(List<String> ids) {
    stashRepository.markStale(ids);
  }

}
