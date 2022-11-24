package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChampionshipStageUpdateDto {
    @NotBlank
    private String name;
    @NotNull
    private Integer duration;
    @NotNull
    private Long startTimestamp;
    @NotBlank
    private String location;
    @NotBlank
    private String description;
    @NotBlank
    private String participationInfo;
    @NotNull
    private Integer attempts;
    @NotNull
    private Integer omt;
}
