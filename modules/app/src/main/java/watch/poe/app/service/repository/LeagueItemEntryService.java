package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import watch.poe.app.utility.GenericsUtility;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.repository.LeagueItemEntryRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueItemEntryService {

  private final LeagueItemEntryRepository itemEntryRepository;

  @Value("${futureHandler.entry.batchSize.stale}")
  private int staleBatchSize;
  @Value("${futureHandler.entry.batchSize.save}")
  private int saveBatchSize;

  @Transactional(propagation = Propagation.REQUIRED)
  public void markStale(List<String> stashIds) {
    // todo: fixme
//    GenericsUtility.toBatches(stashIds, staleBatchSize)
//      .forEach(itemEntryRepository::markStaleByStashIds);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void saveAll(List<LeagueItemEntry> leagueItemEntries) {
    GenericsUtility.toBatches(leagueItemEntries, saveBatchSize)
      .forEach(itemEntryRepository::saveAll);
  }

}
