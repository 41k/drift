package drift.dto;

import drift.model.Discipline;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChampionshipCreationDto {
    @NotBlank
    private String organisationId;
    @NotNull
    private Discipline discipline;
    @NotBlank
    private String scoringSystemId;
}
