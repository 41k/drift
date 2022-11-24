package drift.dto;

import drift.model.Championship;
import drift.model.Discipline;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class ChampionshipDto {

    String id;
    String ownerId;
    String organisationId;
    Discipline discipline;
    String scoringSystemId;
    boolean active;

    public static ChampionshipDto from(Championship championship) {
        return ChampionshipDto.builder()
                .id(championship.getId())
                .ownerId(championship.getOwnerId())
                .organisationId(championship.getOrganisationId())
                .discipline(championship.getDiscipline())
                .scoringSystemId(championship.getScoringSystemId())
                .active(championship.isActive())
                .build();
    }
}
