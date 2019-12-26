package watch.poe.persistence.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class StatisticPk implements Serializable {
    private String type;
    private Date time;
}
