package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.GenericsUtility;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.repository.LeagueItemEntryRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueItemEntryService {

  private final LeagueItemEntryRepository itemEntryRepository;

  @Value("${futureHandler.entry.batchSize}")
  private int batchSize;

  public void markStale(List<String> stashIds) {
    stashIds = stashIds.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());

    var batches = GenericsUtility.toBatches(stashIds, batchSize).collect(Collectors.toList());
    batches.forEach(itemEntryRepository::markStaleByStashIds);
  }

  public void saveAll(List<LeagueItemEntry> leagueItemEntries) {
    itemEntryRepository.saveAll(leagueItemEntries);
  }

}
