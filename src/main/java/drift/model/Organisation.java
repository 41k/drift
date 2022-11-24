package drift.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "organisations")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    @Id
    private String id;
    @NotNull
    private String ownerId;
    @NotNull
    private String name;
    private String description;
    private String image;
    private boolean active;
}
