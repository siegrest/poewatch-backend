package watch.poe.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import watch.poe.persistence.repository.LeagueRepository;

@Service
public class ApiService {

    @Autowired
    public LeagueRepository leagueRepository;

}
