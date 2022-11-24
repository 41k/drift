package drift.dto;

import drift.model.ScoringSystem;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ScoringSystemDto {

    String id;
    String name;
    double participationPoints;
    List<Double> qualificationPoints;
    List<Double> points;
    int participantsAfterQualification;

    public static ScoringSystemDto from(ScoringSystem scoringSystem) {
        return ScoringSystemDto.builder()
                .id(scoringSystem.getId())
                .name(scoringSystem.getName())
                .participationPoints(scoringSystem.getParticipationPoints())
                .qualificationPoints(scoringSystem.getQualificationPoints())
                .points(scoringSystem.getPoints())
                .participantsAfterQualification(scoringSystem.getParticipantsAfterQualification())
                .build();
    }
}
