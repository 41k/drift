package drift.service;

import drift.dto.ScoringSystemCreationDto;
import drift.dto.ScoringSystemDto;
import drift.model.ScoringSystem;
import drift.repository.ScoringSystemRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScoringSystemService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final ScoringSystemRepository scoringSystemRepository;

    public String createScoringSystem(ScoringSystemCreationDto dto) {
        return scoringSystemRepository.save(
                ScoringSystem.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .name(dto.getName())
                        .participationPoints(dto.getParticipationPoints())
                        .qualificationPoints(dto.getQualificationPoints())
                        .points(dto.getPoints())
                        .participantsAfterQualification(dto.getParticipantsAfterQualification())
                        .active(true)
                        .build()
        ).getId();
    }

    public Collection<ScoringSystemDto> getScoringSystems() {
        return scoringSystemRepository.findAllByActive(true).stream()
                .map(ScoringSystemDto::from)
                .collect(Collectors.toList());
    }

    public ScoringSystemDto getScoringSystem(String scoringSystemId) {
        return scoringSystemRepository.findById(scoringSystemId).map(ScoringSystemDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Wrong scoring system id"));
    }
}
