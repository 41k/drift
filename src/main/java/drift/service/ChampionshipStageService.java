package drift.service;

import drift.dto.ChampionshipStageCreationDto;
import drift.dto.ChampionshipStageUpdateDto;
import drift.dto.QualificationResultsDto;
import drift.model.*;
import drift.repository.ChampionshipStageJudgeRepository;
import drift.repository.ChampionshipStageParticipantRepository;
import drift.repository.ChampionshipStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Transactional
@RequiredArgsConstructor
public class ChampionshipStageService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final ChampionshipService championshipService;
    private final UserService userService;
    private final ChampionshipStageRepository championshipStageRepository;
    private final ChampionshipStageParticipantRepository championshipStageParticipantRepository;
    private final ChampionshipStageJudgeRepository championshipStageJudgeRepository;
    private final FileService fileService;

    public String createChampionshipStage(ChampionshipStageCreationDto dto) {
        championshipService.getRequesterChampionship(dto.getChampionshipId());
        return championshipStageRepository.save(
                ChampionshipStage.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .championshipId(dto.getChampionshipId())
                        .name(dto.getName())
                        .duration(dto.getDuration())
                        .startTimestamp(Instant.ofEpochMilli(dto.getStartTimestamp()))
                        .location(dto.getLocation())
                        .description(dto.getDescription())
                        .participationInfo(dto.getParticipationInfo())
                        .attempts(dto.getAttempts())
                        .omt(dto.getOmt())
                        .phase(ChampionshipStagePhase.CREATION)
                        .active(true)
                        .build()
        ).getId();
    }

    public void updateChampionshipStage(String championshipStageId, ChampionshipStageUpdateDto dto) {
        var championshipStage = getRequesterChampionshipStage(championshipStageId);
        validateBeforeUpdate(championshipStage);
        championshipStageRepository.save(
                championshipStage.toBuilder()
                        .name(dto.getName())
                        .duration(dto.getDuration())
                        .startTimestamp(Instant.ofEpochMilli(dto.getStartTimestamp()))
                        .location(dto.getLocation())
                        .description(dto.getDescription())
                        .participationInfo(dto.getParticipationInfo())
                        .attempts(dto.getAttempts())
                        .omt(dto.getOmt())
                        .build());
    }

    public void deactivateChampionshipStage(String championshipStageId) {
        var championshipStage = getRequesterChampionshipStage(championshipStageId);
        championshipStageRepository.save(championshipStage.toBuilder().active(false).build());
    }

    public void uploadPlacardImage(String championshipStageId, MultipartFile image) {
        var championshipStage = getRequesterChampionshipStage(championshipStageId);
        validateBeforeUpdate(championshipStage);
        var imagePath = fileService.uploadImage(ImageCategory.CHAMPIONSHIP_STAGE, championshipStageId, image);
        var updatedChampionshipStage = championshipStage.toBuilder().placardImage(imagePath).build();
        championshipStageRepository.save(updatedChampionshipStage);
    }

    public void registerParticipant(String championshipStageId) {
        securityService.validateRequesterRoles(Role.DRIVER);
        getChampionshipStage(championshipStageId);
        var userId = securityService.getRequesterId();
        if (championshipStageParticipantRepository.existsByChampionshipStageIdAndUserId(championshipStageId, userId)) {
            return;
        }
        championshipStageParticipantRepository.save(
                ChampionshipStageParticipant.builder()
                        .id(idGenerator.generate())
                        .championshipStageId(championshipStageId)
                        .userId(userId)
                        .build());
    }

    public void deleteParticipant(String championshipStageId) {
        getChampionshipStage(championshipStageId);
        var userId = securityService.getRequesterId();
        championshipStageParticipantRepository.deleteByChampionshipStageIdAndUserId(championshipStageId, userId);
    }

    public Collection<String> getParticipants(String championshipStageId) {
        getChampionshipStage(championshipStageId);
        return championshipStageParticipantRepository.findAllByChampionshipStageId(championshipStageId).stream()
                .map(ChampionshipStageParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    public void assignJudges(String championshipStageId, Collection<String> userIds) {
        getRequesterChampionshipStage(championshipStageId);
        if (userIds.size() < 1 || userIds.size() > 3) {
            throw new IllegalArgumentException("Wrong number of judges");
        }
        userIds.forEach(userService::getUser);
        championshipStageJudgeRepository.deleteAllByChampionshipStageId(championshipStageId);
        var judges = userIds.stream()
                .map(userId ->
                        ChampionshipStageJudge.builder()
                                .id(idGenerator.generate())
                                .championshipStageId(championshipStageId)
                                .userId(userId)
                                .build())
                .collect(Collectors.toList());
        championshipStageJudgeRepository.saveAll(judges);
    }

    public Collection<String> getJudges(String championshipStageId) {
        getChampionshipStage(championshipStageId);
        return championshipStageJudgeRepository.findAllByChampionshipStageId(championshipStageId).stream()
                .map(ChampionshipStageJudge::getUserId)
                .collect(Collectors.toSet());
    }

    public void startQualification(String championshipStageId) {
        var championshipStage = getRequesterChampionshipStage(championshipStageId);
        validateBeforeQualificationStart(championshipStage);
        initQualificationResults(championshipStage);
        championshipStageRepository.save(
                championshipStage.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build());
    }

    public void updateQualificationResults(String championshipStageId, QualificationResultsDto qualificationResults) {
        validateBeforeQualificationResultsUpdate(championshipStageId, qualificationResults);
        var participantUserId = qualificationResults.getParticipantUserId();
        var judgeUserId = qualificationResults.getJudgeUserId();
        var attemptsPoints = qualificationResults.getAttemptsPoints();
        championshipStageParticipantRepository.findByChampionshipStageIdAndUserId(championshipStageId, participantUserId)
                .map(participant -> {
                    participant.getQualificationResults().put(judgeUserId, attemptsPoints);
                    return participant;
                })
                .map(championshipStageParticipantRepository::save)
                .orElseThrow();
    }

    public Map<String, List<Double>> getQualificationResults(String championshipStageId, String judgeUserId) {
        getChampionshipStage(championshipStageId);
        if (!championshipStageJudgeRepository.existsByChampionshipStageIdAndUserId(championshipStageId, judgeUserId)) {
            throw new IllegalArgumentException("Wrong judge user id");
        }
        return championshipStageParticipantRepository.findAllByChampionshipStageId(championshipStageId).stream()
                .collect(Collectors.toMap(
                        ChampionshipStageParticipant::getUserId,
                        participant -> participant.getQualificationResults().get(judgeUserId)));
    }

    public Map<String, Double> getQualificationResults(String championshipStageId) {
        var championshipStage = getChampionshipStage(championshipStageId);
        return championshipStageParticipantRepository.findAllByChampionshipStageId(championshipStageId).stream()
                .collect(Collectors.toMap(
                        ChampionshipStageParticipant::getUserId,
                        participant -> calculateBestAttemptResult(participant, championshipStage)));
    }

    private void validateBeforeQualificationStart(ChampionshipStage championshipStage) {
        if (!ChampionshipStagePhase.CREATION.equals(championshipStage.getPhase())) {
            throw new IllegalArgumentException("Qualification can be started from CREATION phase only");
        }
        if (championshipStageJudgeRepository.countByChampionshipStageId(championshipStage.getId()) == 0) {
            throw new IllegalArgumentException("Judges are not assigned");
        }
        if (championshipStageParticipantRepository.countByChampionshipStageId(championshipStage.getId()) == 0) {
            throw new IllegalArgumentException("No participants");
        }
    }

    private void initQualificationResults(ChampionshipStage championshipStage) {
        var judges = championshipStageJudgeRepository.findAllByChampionshipStageId(championshipStage.getId());
        var attemptsPoints = Collections.nCopies(championshipStage.getAttempts(), 0d);
        var participantsWithQualificationResults = championshipStageParticipantRepository.findAllByChampionshipStageId(championshipStage.getId()).stream()
                .map(participant -> {
                    var qualificationResults = judges.stream()
                            .collect(Collectors.toMap(ChampionshipStageJudge::getUserId, judge -> attemptsPoints));
                    return participant.toBuilder().qualificationResults(qualificationResults).build();
                })
                .collect(Collectors.toList());
        championshipStageParticipantRepository.saveAll(participantsWithQualificationResults);
    }

    private void validateBeforeQualificationResultsUpdate(
            String championshipStageId,
            QualificationResultsDto qualificationResults
    ) {
        var championshipStage = getChampionshipStage(championshipStageId);
        if (!ChampionshipStagePhase.QUALIFICATION.equals(championshipStage.getPhase())) {
            throw new IllegalArgumentException("Qualification results can be updated during QUALIFICATION phase only");
        }
        if (!championshipStageJudgeRepository.existsByChampionshipStageIdAndUserId(championshipStage.getId(), qualificationResults.getJudgeUserId())) {
            throw new IllegalArgumentException("Wrong judge user id");
        }
        if (!championshipStageParticipantRepository.existsByChampionshipStageIdAndUserId(championshipStage.getId(), qualificationResults.getParticipantUserId())) {
            throw new IllegalArgumentException("Wrong participant user id");
        }
        if (qualificationResults.getAttemptsPoints().size() != championshipStage.getAttempts()) {
            throw new IllegalArgumentException("Wrong number of attempts");
        }
    }

    private Double calculateBestAttemptResult(ChampionshipStageParticipant participant, ChampionshipStage championshipStage) {
        return IntStream.range(0, championshipStage.getAttempts())
                .mapToObj(attemptIndex ->
                        participant.getQualificationResults().values().stream()
                                .map(attemptsPointsByJudge -> attemptsPointsByJudge.get(attemptIndex))
                                .reduce(0d, Double::sum))
                .max(Double::compareTo)
                .orElseThrow();
    }

    private ChampionshipStage getRequesterChampionshipStage(String championshipStageId) {
        var championshipStage = getChampionshipStage(championshipStageId);
        securityService.validateOwner(championshipStage.getOwnerId(), ChampionshipStage.class);
        return championshipStage;
    }

    private ChampionshipStage getChampionshipStage(String championshipStageId) {
        return championshipStageRepository.findById(championshipStageId).orElseThrow(wrongChampionshipStageIdException());
    }

    private Supplier<IllegalArgumentException> wrongChampionshipStageIdException() {
        return () -> new IllegalArgumentException("Wrong championship stage id");
    }

    private void validateBeforeUpdate(ChampionshipStage championshipStage) {
        var phase = championshipStage.getPhase();
        if (!ChampionshipStagePhase.CREATION.equals(phase)) {
            throw new IllegalArgumentException(
                    String.format("Championship stage update is prohibited during %s phase", phase));
        }
    }
}
