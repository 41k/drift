package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
public class ScoringSystemCreationDto {
    @NotBlank
    private String name;
    @PositiveOrZero
    private double participationPoints;
    @NotEmpty
    private List<Double> qualificationPoints;
    @NotEmpty
    private List<Double> points;
    @Positive
    private int participantsAfterQualification;
}
