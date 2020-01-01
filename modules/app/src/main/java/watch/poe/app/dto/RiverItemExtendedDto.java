package watch.poe.app.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class RiverItemExtendedDto {
    private String category;
    private List<String> subcategories;
}
