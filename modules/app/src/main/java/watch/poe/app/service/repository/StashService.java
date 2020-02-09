package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.GenericsUtility;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.repository.StashRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StashService {

  public final StashRepository stashRepository;

  @Value("${futureHandler.stash.batchSize.save}")
  private int saveBatchSize;

  public List<Stash> saveAll(List<Stash> stashes) {
    return GenericsUtility.toBatches(stashes, saveBatchSize)
      .map(stashRepository::saveAll)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  public void markStale(List<Long> ids) {
    stashRepository.markStale(ids);
  }

}
