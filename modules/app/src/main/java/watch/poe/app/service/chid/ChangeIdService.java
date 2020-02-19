package watch.poe.app.service.chid;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.domain.ChangeIdId;
import watch.poe.persistence.model.ChangeId;
import watch.poe.persistence.repository.ChangeIdRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeIdService {

  private final ChangeIdRepository changeIdRepository;

  public void saveIfNewer(ChangeIdId id, String changeId) {
    var job = changeIdRepository.findById(id);
    if (job.isEmpty() || ChangeIdUtility.isNewerThan(changeId, job.get().getChangeId())) {
      save(id, changeId);
    }
  }

  public ChangeId save(ChangeIdId id, String changeIdString) {
    var changeId = ChangeId.builder()
      .id(id)
      .changeId(changeIdString)
      .build();
    return changeIdRepository.save(changeId);
  }

  public Optional<ChangeId> find(ChangeIdId id) {
    return changeIdRepository.findById(id);
  }

}
