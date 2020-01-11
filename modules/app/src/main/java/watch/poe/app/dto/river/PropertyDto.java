package watch.poe.app.dto.river;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PropertyDto {
    private String name;
    private List<List<String>> values;
}
