package watch.poe.persistence.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category", schema = "pw")
public class Category {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(length = 32, nullable = false, unique = true)
    private String name;

    @Column(length = 32, nullable = false)
    private String display;
}
