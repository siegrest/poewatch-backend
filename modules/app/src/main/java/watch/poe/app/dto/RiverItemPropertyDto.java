package watch.poe.app.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class RiverItemPropertyDto {
    private String name;
    private List<List<String>> values;
}
