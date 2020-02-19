package watch.poe.app.dto.resource;

import lombok.Getter;

import java.util.List;

@Getter
public class GroupMappingDto {
    private String category;
    private List<String> groups;
}
