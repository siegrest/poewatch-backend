package watch.poe.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PriceDto {
    private String currencyName;
    private double price;
}
