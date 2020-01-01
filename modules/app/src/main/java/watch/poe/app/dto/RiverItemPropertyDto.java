package watch.poe.app.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class RiverItemPropertyDto {
    private String name;
    private List<List<String>> values;
}
