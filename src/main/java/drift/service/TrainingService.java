package drift.service;

import drift.dto.TrainingCreationDto;
import drift.dto.TrainingUpdateDto;
import drift.model.ImageCategory;
import drift.model.Role;
import drift.model.Training;
import drift.model.TrainingParticipant;
import drift.repository.TrainingParticipantRepository;
import drift.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
public class TrainingService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final OrganisationService organisationService;
    private final TrainingRepository trainingRepository;
    private final TrainingParticipantRepository trainingParticipantRepository;
    private final FileService fileService;

    public String createTraining(TrainingCreationDto dto) {
        organisationService.getRequesterOrganisation(dto.getOrganisationId());
        return trainingRepository.save(
                Training.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .organisationId(dto.getOrganisationId())
                        .discipline(dto.getDiscipline())
                        .name(dto.getName())
                        .startTimestamp(Instant.ofEpochMilli(dto.getStartTimestamp()))
                        .location(dto.getLocation())
                        .participationInfo(dto.getParticipationInfo())
                        .active(true)
                        .build()
        ).getId();
    }

    public void updateTraining(String trainingId, TrainingUpdateDto dto) {
        var training = getRequesterTraining(trainingId);
        trainingRepository.save(
                training.toBuilder()
                        .name(dto.getName())
                        .startTimestamp(Instant.ofEpochMilli(dto.getStartTimestamp()))
                        .location(dto.getLocation())
                        .participationInfo(dto.getParticipationInfo())
                        .build());
    }

    public void deactivateTraining(String trainingId) {
        var training = getRequesterTraining(trainingId);
        trainingRepository.save(training.toBuilder().active(false).build());
    }

    public void uploadPlacardImage(String trainingId, MultipartFile image) {
        var training = getRequesterTraining(trainingId);
        var imagePath = fileService.uploadImage(ImageCategory.TRAINING, trainingId, image);
        var updatedTraining = training.toBuilder().placardImage(imagePath).build();
        trainingRepository.save(updatedTraining);
    }

    public void registerParticipant(String trainingId) {
        securityService.validateRequesterRoles(Role.DRIVER);
        getTraining(trainingId);
        var userId = securityService.getRequesterId();
        if (trainingParticipantRepository.existsByTrainingIdAndUserId(trainingId, userId)) {
            return;
        }
        trainingParticipantRepository.save(
                TrainingParticipant.builder()
                        .id(idGenerator.generate())
                        .trainingId(trainingId)
                        .userId(userId)
                        .build());
    }

    public void deleteParticipant(String trainingId) {
        getTraining(trainingId);
        var userId = securityService.getRequesterId();
        trainingParticipantRepository.deleteByTrainingIdAndUserId(trainingId, userId);
    }

    public Collection<String> getParticipants(String trainingId) {
        getTraining(trainingId);
        return trainingParticipantRepository.findAllByTrainingId(trainingId).stream()
                .map(TrainingParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    private Training getRequesterTraining(String trainingId) {
        var training = getTraining(trainingId);
        securityService.validateOwner(training.getOwnerId(), Training.class);
        return training;
    }

    private Training getTraining(String trainingId) {
        return trainingRepository.findById(trainingId).orElseThrow(wrongTrainingIdException());
    }

    private Supplier<IllegalArgumentException> wrongTrainingIdException() {
        return () -> new IllegalArgumentException("Wrong training id");
    }
}
