package watch.poe.app.service.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import watch.poe.app.dto.resource.CurrencyAliasDto;
import watch.poe.app.service.GsonService;
import watch.poe.app.utility.FileUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyAliasService {

    private static final String FILE_LOCATION = "classpath:currency_aliases.json";
    private static final List<CurrencyAliasDto> currencies = new ArrayList<>();

  private final GsonService gsonService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadAliases() {
        var json = FileUtility.loadFile(FILE_LOCATION);
        var currencyAliasDtoList = gsonService.toList(json, CurrencyAliasDto.class);
        currencies.addAll(currencyAliasDtoList);
    }

    public boolean isValidAlias(String alias) {
        return getCurrency(alias).isPresent();
    }

    public Optional<CurrencyAliasDto> getCurrency(String alias) {
        return currencies.stream()
                .filter(c -> c.getAliases().stream().anyMatch(a -> a.equals(alias)))
                .findFirst();
    }

}
