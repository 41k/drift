package drift.dto;

import drift.model.Discipline;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TrainingCreationDto {
    @NotBlank
    private String organisationId;
    @NotNull
    private Discipline discipline;
    @NotBlank
    private String name;
    @NotNull
    private Long startTimestamp;
    @NotBlank
    private String location;
    @NotBlank
    private String participationInfo;
}
