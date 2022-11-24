package drift.model;

import drift.repository.ListOfDoublesToStringConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "scoring_systems")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScoringSystem {
    @Id
    private String id;
    @NotNull
    private String ownerId;
    @NotNull
    private String name;
    @NotNull
    private Double participationPoints;
    @NotEmpty
    @Convert(converter = ListOfDoublesToStringConverter.class)
    private List<Double> qualificationPoints;
    @NotEmpty
    @Convert(converter = ListOfDoublesToStringConverter.class)
    private List<Double> points;
    @NotNull
    private Integer participantsAfterQualification;
    private boolean active;
}
