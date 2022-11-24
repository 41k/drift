package drift.dto;

import drift.model.ChampionshipStage;
import drift.model.Discipline;
import drift.model.Training;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class TrainingDto {

    String id;
    String ownerId;
    String organisationId;
    Discipline discipline;
    String name;
    Instant startTimestamp;
    String location;
    String participationInfo;
    String placardImage;
    boolean active;

    public static TrainingDto from(Training training) {
        return TrainingDto.builder()
                .id(training.getId())
                .ownerId(training.getOwnerId())
                .organisationId(training.getOrganisationId())
                .discipline(training.getDiscipline())
                .name(training.getName())
                .startTimestamp(training.getStartTimestamp())
                .location(training.getLocation())
                .participationInfo(training.getParticipationInfo())
                .placardImage(training.getPlacardImage())
                .active(training.isActive())
                .build();
    }
}
