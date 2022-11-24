package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;

@Data
public class ChampionshipStageJudgesAssignmentDto {
    @NotEmpty
    private Collection<String> userIds;
}
