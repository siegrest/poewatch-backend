package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.repository.LeagueItemEntryRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueItemEntryService {

  private final LeagueItemEntryRepository itemEntryRepository;

  public void markStale(List<String> ids) {
    ids.forEach(itemEntryRepository::markStale);
  }

  public void saveAll(List<LeagueItemEntry> leagueItemEntries) {
    itemEntryRepository.saveAll(leagueItemEntries);
  }

}
