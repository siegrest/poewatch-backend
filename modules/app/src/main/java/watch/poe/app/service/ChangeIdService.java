package watch.poe.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import watch.poe.app.utility.ChangeIdUtility;
import watch.poe.persistence.model.changeId.ChangeId;
import watch.poe.persistence.model.changeId.ChangeIdType;
import watch.poe.persistence.repository.ChangeIdRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeIdService {

  private final ChangeIdRepository changeIdRepository;

  public void saveIfNewer(ChangeIdType type, String value) {
    changeIdRepository.findById(type).ifPresent(changeId -> {
      if (ChangeIdUtility.isNewerThan(value, changeId.getValue())) {
        save(type, value);
      }
    });
  }

  public ChangeId save(ChangeIdType type, String value) {
    var changeId = ChangeId.builder()
      .type(type)
      .value(value)
      .time(LocalDateTime.now())
      .build();
    return changeIdRepository.save(changeId);
  }

  public Optional<ChangeId> findById(ChangeIdType id) {
    return changeIdRepository.findById(id);
  }

}
