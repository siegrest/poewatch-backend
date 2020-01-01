package watch.poe.app.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.RiverStashDto;
import watch.poe.persistence.model.Stash;
import watch.poe.persistence.repository.StashRepository;

import java.util.Optional;

@Slf4j
@Service
public class StashRepositoryService {

    @Autowired
    public StashRepository stashRepository;

    public Optional<Stash> findByStashId(String stashId) {
        return stashRepository.findByStashId(stashId);
    }

    public void create(RiverStashDto riverStashDto) {
        // todo: this
    }

}
