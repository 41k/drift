package drift.model;

import drift.repository.StringToListOfDoublesMapToStringConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "championship_stage_participants")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipStageParticipant {
    @Id
    private String id;
    @NotNull
    private String championshipStageId;
    @NotNull
    private String userId;
    @Convert(converter = StringToListOfDoublesMapToStringConverter.class)
    private Map<String, List<Double>> qualificationResults;
}
