package watch.poe.app.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class RiverItemPropertyDto {
    private String name;
    private List<List<String>> values;
}
