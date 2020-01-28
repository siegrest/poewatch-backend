package watch.poe.app.service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.ChangeId;
import watch.poe.persistence.repository.ChangeIdRepository;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeIdService {

  public static final String RIVER = "river";

  private final ChangeIdRepository changeIdRepository;

  public void saveRiverIfNew(String changeId) {
    var repoJob = get(RIVER);

    if (repoJob.isEmpty() || ChangeIdUtility.isNewerThan(changeId, repoJob.get().getChangeId())) {
      save(RIVER, changeId);
    }
  }

  public ChangeId save(String id, String changeIdString) {
    var changeId = ChangeId.builder()
      .id(id)
      .changeId(changeIdString)
      .time(new Date())
      .build();
    return changeIdRepository.save(changeId);
  }

  public Optional<ChangeId> get(String id) {
    return changeIdRepository.findById(id);
  }

}
