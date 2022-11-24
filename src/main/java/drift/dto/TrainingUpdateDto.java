package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TrainingUpdateDto {
    @NotBlank
    private String name;
    @NotNull
    private Long startTimestamp;
    @NotBlank
    private String location;
    @NotBlank
    private String participationInfo;
}
