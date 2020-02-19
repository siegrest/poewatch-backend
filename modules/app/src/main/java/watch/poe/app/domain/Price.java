package watch.poe.app.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Price {
    private String currencyName;
    private double price;
}
