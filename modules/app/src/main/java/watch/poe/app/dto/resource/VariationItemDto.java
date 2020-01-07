package watch.poe.app.dto.resource;

import lombok.Getter;

import java.util.List;

@Getter
public class VariationItemDto {
    private String name;
    private List<VariationDto> variations;
}
