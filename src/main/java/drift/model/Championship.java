package drift.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "championships")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Championship {
    @Id
    private String id;
    @NotNull
    private String ownerId;
    @NotNull
    private String organisationId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Discipline discipline;
    @NotNull
    private String scoringSystemId;
    private boolean active;
}
