package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class QualificationResultsDto {
    @NotBlank
    String judgeUserId;
    @NotBlank
    String participantUserId;
    @NotEmpty
    List<Double> attemptsPoints;
}
