package drift.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "championship_stages")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipStage {
    @Id
    private String id;
    @NotNull
    private String ownerId;
    @NotNull
    private String championshipId;
    @NotNull
    private String name;
    @NotNull
    private Integer duration;
    @NotNull
    private Instant startTimestamp;
    @NotNull
    private String location;
    @NotNull
    private String description;
    @NotNull
    private String participationInfo;
    @NotNull
    private Integer attempts;
    @NotNull
    private Integer omt;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ChampionshipStagePhase phase;
    private String placardImage;
    private boolean active;
}
