package watch.poe.app.dto.river;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ExtendedDto {
    private String category;
    private List<String> subcategories;
}
