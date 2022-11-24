package drift.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "trainings")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Training {
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
    private String name;
    @NotNull
    private Instant startTimestamp;
    @NotNull
    private String location;
    @NotNull
    private String participationInfo;
    private String placardImage;
    private boolean active;
}
