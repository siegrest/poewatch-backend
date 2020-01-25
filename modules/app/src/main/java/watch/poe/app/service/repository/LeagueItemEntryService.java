package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.LeagueItemEntry;
import watch.poe.persistence.repository.LeagueItemEntryRepository;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueItemEntryService {

  private final LeagueItemEntryRepository itemEntryRepository;

  public LeagueItemEntry save(LeagueItemEntry newEntry) {
    var dbEntry = itemEntryRepository.findById(newEntry.getId());
    if (dbEntry.isEmpty()) {
      return saveNew(newEntry);
    }

    return update(newEntry, dbEntry.get());
  }

  private LeagueItemEntry saveNew(LeagueItemEntry entry) {
    entry.setFound(new Date());
    entry.setSeen(new Date());
    entry.setUpdates(0);
    return itemEntryRepository.save(entry);
  }

  private LeagueItemEntry update(LeagueItemEntry newEntry, LeagueItemEntry dbEntry) {
    dbEntry.setSeen(new Date());
    dbEntry.setUpdates(newEntry.getUpdates() + 1);
    dbEntry.setStackSize(newEntry.getStackSize());
    dbEntry.setPrice(newEntry.getPrice());
    dbEntry.setPriceItem(newEntry.getPriceItem());
    dbEntry.setStash(newEntry.getStash());
    return itemEntryRepository.save(dbEntry);
  }

}
