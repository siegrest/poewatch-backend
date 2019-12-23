package watch.poe.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.model.ChangeId;
import watch.poe.persistence.repository.ChangeIdRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class ChangeIdService {

    public static final String RIVER = "river";

    @Autowired
    private ChangeIdRepository changeIdRepository;

    public ChangeId save(String name, String changeIdString) {
        var changeId = ChangeId.builder()
                .name(name)
                .changeId(changeIdString)
                .time(new Date())
                .build();
        return changeIdRepository.save(changeId);
    }

    public void update(String name, String changeId) {
        changeIdRepository.updateByName(name, changeId);
    }

    public Optional<ChangeId> get(String name) {
        return changeIdRepository.getByName(name);
    }

    public Optional<ChangeId> get(Integer id) {
        return changeIdRepository.getById(id);
    }

}
