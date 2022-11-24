package drift.dto;

import drift.model.ChampionshipStage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class ChampionshipStageDto {

    String id;
    String ownerId;
    String championshipId;
    String name;
    Integer duration;
    Instant startTimestamp;
    String location;
    String description;
    String participationInfo;
    Integer attempts;
    Integer omt;
    String placardImage;
    boolean active;

    public static ChampionshipStageDto from(ChampionshipStage championshipStage) {
        return ChampionshipStageDto.builder()
                .id(championshipStage.getId())
                .ownerId(championshipStage.getOwnerId())
                .championshipId(championshipStage.getChampionshipId())
                .name(championshipStage.getName())
                .duration(championshipStage.getDuration())
                .startTimestamp(championshipStage.getStartTimestamp())
                .location(championshipStage.getLocation())
                .description(championshipStage.getDescription())
                .participationInfo(championshipStage.getParticipationInfo())
                .attempts(championshipStage.getAttempts())
                .omt(championshipStage.getOmt())
                .placardImage(championshipStage.getPlacardImage())
                .active(championshipStage.isActive())
                .build();
    }
}
