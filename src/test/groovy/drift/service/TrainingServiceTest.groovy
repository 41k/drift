package drift.service


import drift.model.ImageCategory
import drift.model.Role
import drift.model.Training
import drift.model.TrainingParticipant
import drift.repository.TrainingParticipantRepository
import drift.repository.TrainingRepository
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static drift.util.TestConstants.*

class TrainingServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private organisationService = Mock(OrganisationService)
    private trainingRepository = Mock(TrainingRepository)
    private trainingParticipantRepository = Mock(TrainingParticipantRepository)
    private fileService = Mock(FileService)
    private image = Mock(MultipartFile)

    private trainingService = new TrainingService(
            idGenerator, securityService, organisationService,
            trainingRepository, trainingParticipantRepository, fileService)


    // --- Training creation

    def 'should create training successfully'() {
        when:
        def trainingId = trainingService.createTraining(TRAINING_CREATION_DTO)

        then:
        1 * organisationService.getRequesterOrganisation(ORGANISATION_ID)
        1 * idGenerator.generate() >> TRAINING_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * trainingRepository.save(TRAINING) >> TRAINING
        0 * _

        and:
        trainingId == TRAINING_ID
    }

    def 'should not create training if wrong organisation id is provided'() {
        when:
        trainingService.createTraining(TRAINING_CREATION_DTO)

        then:
        1 * organisationService.getRequesterOrganisation(ORGANISATION_ID) >> { throw new IllegalArgumentException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Training update

    def 'should update training successfully'() {
        given:
        def updatedTraining = TRAINING.toBuilder()
                .name(NAME_2).startTimestamp(TIMESTAMP_2_AS_INSTANT)
                .location(LOCATION_2).participationInfo(PARTICIPATION_INFO_2).build()
        when:
        trainingService.updateTraining(TRAINING_ID, TRAINING_UPDATE_DTO)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training)
        1 * trainingRepository.save(updatedTraining)
        0 * _
    }

    def 'should throw exception during training update if training is not found by provided id'() {
        when:
        trainingService.updateTraining(TRAINING_ID, TRAINING_UPDATE_DTO)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }

    def 'should throw exception during training update if requester is not owner'() {
        when:
        trainingService.updateTraining(TRAINING_ID, TRAINING_UPDATE_DTO)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Training deactivation

    def 'should deactivate training successfully'() {
        given:
        def deactivatedTraining = TRAINING.toBuilder().active(false).build()

        when:
        trainingService.deactivateTraining(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training)
        1 * trainingRepository.save(deactivatedTraining)
        0 * _
    }

    def 'should throw exception during training deactivation if training is not found by provided id'() {
        when:
        trainingService.deactivateTraining(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }

    def 'should throw exception during training deactivation if requester is not owner'() {
        when:
        trainingService.deactivateTraining(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Placard image upload

    def 'should upload placard image successfully'() {
        given:
        def updatedTraining = TRAINING.toBuilder().placardImage(PATH).build()

        when:
        trainingService.uploadPlacardImage(TRAINING_ID, image)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training)
        1 * fileService.uploadImage(ImageCategory.TRAINING, TRAINING_ID, image) >> PATH
        1 * trainingRepository.save(updatedTraining)
        0 * _
    }

    def 'should throw exception during placard image upload if training is not found by provided id'() {
        when:
        trainingService.uploadPlacardImage(TRAINING_ID, image)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }

    def 'should throw exception during placard image upload if requester is not owner'() {
        when:
        trainingService.uploadPlacardImage(TRAINING_ID, image)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.validateOwner(USER_ID_1, Training) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Participant registration

    def 'should register requester as participant successfully'() {
        when:
        trainingService.registerParticipant(TRAINING_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * trainingParticipantRepository.existsByTrainingIdAndUserId(TRAINING_ID, USER_ID_1) >> false
        1 * idGenerator.generate() >> TRAINING_PARTICIPANT_ID
        1 * trainingParticipantRepository.save(TRAINING_PARTICIPANT)
        0 * _
    }

    def 'should not register participant if requester is not DRIVER'() {
        when:
        trainingService.registerParticipant(TRAINING_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should not register participant if wrong training id is provided'() {
        when:
        trainingService.registerParticipant(TRAINING_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }

    def 'should not register the same participant'() {
        when:
        trainingService.registerParticipant(TRAINING_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * trainingParticipantRepository.existsByTrainingIdAndUserId(TRAINING_ID, USER_ID_1) >> true
        0 * _
    }


    // --- Participant deletion

    def 'should delete participant successfully'() {
        when:
        trainingService.deleteParticipant(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * trainingParticipantRepository.deleteByTrainingIdAndUserId(TRAINING_ID, USER_ID_1)
        0 * _
    }

    def 'should not delete participant if wrong training id is provided'() {
        when:
        trainingService.deleteParticipant(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }


    // --- Participants retrieval

    def 'should provide training participants'() {
        given:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.of(TRAINING)
        1 * trainingParticipantRepository.findAllByTrainingId(TRAINING_ID) >> [
                TrainingParticipant.builder().userId(USER_ID_1).build(),
                TrainingParticipant.builder().userId(USER_ID_2).build()
        ]
        0 * _

        expect:
        trainingService.getParticipants(TRAINING_ID) == [USER_ID_1, USER_ID_2] as Set
    }

    def 'should not provide participants if wrong training id is provided'() {
        when:
        trainingService.getParticipants(TRAINING_ID)

        then:
        1 * trainingRepository.findById(TRAINING_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong training id'
    }
}
