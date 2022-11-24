package drift.repository;

import drift.model.Discipline;
import lombok.Data;

import java.util.Collection;

@Data
public class ChampionshipsSearchContext {
    private Collection<String> championshipIds;
    private Collection<String> ownerIds;
    private Collection<String> organisationIds;
    private Collection<Discipline> disciplines;
    private Boolean active;
}
