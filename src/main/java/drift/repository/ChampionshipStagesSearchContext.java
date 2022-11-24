package drift.repository;

import lombok.Data;

import java.util.Collection;

@Data
public class ChampionshipStagesSearchContext {
    private Collection<String> championshipIds;
    private Collection<String> championshipStageIds;
    private Boolean active;
}
