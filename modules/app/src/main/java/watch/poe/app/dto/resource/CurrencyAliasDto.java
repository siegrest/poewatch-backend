package watch.poe.app.dto.resource;

import lombok.Getter;

import java.util.List;

@Getter
public class CurrencyAliasDto {
    private String name;
    private List<String> aliases;
}
