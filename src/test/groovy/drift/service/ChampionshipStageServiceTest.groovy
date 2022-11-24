package drift.service

import drift.model.*
import drift.repository.ChampionshipStageJudgeRepository
import drift.repository.ChampionshipStageParticipantRepository
import drift.repository.ChampionshipStageRepository
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static drift.util.TestConstants.*

class ChampionshipStageServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private championshipService = Mock(ChampionshipService)
    private userService = Mock(UserService)
    private championshipStageRepository = Mock(ChampionshipStageRepository)
    private championshipStageParticipantRepository = Mock(ChampionshipStageParticipantRepository)
    private championshipStageJudgeRepository = Mock(ChampionshipStageJudgeRepository)
    private fileService = Mock(FileService)
    private image = Mock(MultipartFile)

    private championshipStageService = new ChampionshipStageService(
            idGenerator, securityService, championshipService, userService, championshipStageRepository,
            championshipStageParticipantRepository, championshipStageJudgeRepository, fileService)


    // --- Championship stage creation

    def 'should create championship stage successfully'() {
        when:
        def championshipStageId = championshipStageService.createChampionshipStage(CHAMPIONSHIP_STAGE_CREATION_DTO)

        then:
        1 * championshipService.getRequesterChampionship(CHAMPIONSHIP_ID)
        1 * idGenerator.generate() >> CHAMPIONSHIP_STAGE_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * championshipStageRepository.save(CHAMPIONSHIP_STAGE) >> CHAMPIONSHIP_STAGE
        0 * _

        and:
        championshipStageId == CHAMPIONSHIP_STAGE_ID
    }

    def 'should not create championship stage if wrong championship id is provided'() {
        when:
        championshipStageService.createChampionshipStage(CHAMPIONSHIP_STAGE_CREATION_DTO)

        then:
        1 * championshipService.getRequesterChampionship(CHAMPIONSHIP_ID) >> { throw new IllegalArgumentException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Championship stage update

    def 'should update championship stage successfully'() {
        given:
        def updatedChampionshipStage = CHAMPIONSHIP_STAGE.toBuilder()
                .name(NAME_2).duration(DURATION_2).startTimestamp(TIMESTAMP_2_AS_INSTANT)
                .location(LOCATION_2).description(DESCRIPTION_2).participationInfo(PARTICIPATION_INFO_2)
                .attempts(N_ATTEMPTS_2).omt(N_OMT_2).active(true).build()
        when:
        championshipStageService.updateChampionshipStage(CHAMPIONSHIP_STAGE_ID, CHAMPIONSHIP_STAGE_UPDATE_DTO)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * championshipStageRepository.save(updatedChampionshipStage)
        0 * _
    }

    def 'should throw exception during championship stage update if championship stage is not found by provided id'() {
        when:
        championshipStageService.updateChampionshipStage(CHAMPIONSHIP_STAGE_ID, CHAMPIONSHIP_STAGE_UPDATE_DTO)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should throw exception during championship stage update if requester is not owner'() {
        when:
        championshipStageService.updateChampionshipStage(CHAMPIONSHIP_STAGE_ID, CHAMPIONSHIP_STAGE_UPDATE_DTO)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should throw exception during championship stage update if it is not in CREATION phase'() {
        when:
        championshipStageService.updateChampionshipStage(CHAMPIONSHIP_STAGE_ID, CHAMPIONSHIP_STAGE_UPDATE_DTO)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Championship stage update is prohibited during QUALIFICATION phase'
    }


    // --- Championship stage deactivation

    def 'should deactivate championship stage successfully'() {
        given:
        def deactivatedChampionshipStage = CHAMPIONSHIP_STAGE.toBuilder().active(false).build()

        when:
        championshipStageService.deactivateChampionshipStage(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * championshipStageRepository.save(deactivatedChampionshipStage)
        0 * _
    }

    def 'should throw exception during championship stage deactivation if championship stage is not found by provided id'() {
        when:
        championshipStageService.deactivateChampionshipStage(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should throw exception during championship stage deactivation if requester is not owner'() {
        when:
        championshipStageService.deactivateChampionshipStage(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Placard image upload

    def 'should upload placard image successfully'() {
        given:
        def updatedChampionshipStage = CHAMPIONSHIP_STAGE.toBuilder().placardImage(PATH).build()

        when:
        championshipStageService.uploadPlacardImage(CHAMPIONSHIP_STAGE_ID, image)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * fileService.uploadImage(ImageCategory.CHAMPIONSHIP_STAGE, CHAMPIONSHIP_STAGE_ID, image) >> PATH
        1 * championshipStageRepository.save(updatedChampionshipStage)
        0 * _
    }

    def 'should throw exception during placard image upload if championship stage is not found by provided id'() {
        when:
        championshipStageService.uploadPlacardImage(CHAMPIONSHIP_STAGE_ID, image)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should throw exception during placard image upload if requester is not owner'() {
        when:
        championshipStageService.uploadPlacardImage(CHAMPIONSHIP_STAGE_ID, image)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should throw exception during placard image upload if championship stage is not in CREATION phase'() {
        when:
        championshipStageService.uploadPlacardImage(CHAMPIONSHIP_STAGE_ID, image)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.PAIRS_RACES).build())
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Championship stage update is prohibited during PAIRS_RACES phase'
    }


    // --- Participant registration

    def 'should register requester as participant successfully'() {
        when:
        championshipStageService.registerParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * championshipStageParticipantRepository.existsByChampionshipStageIdAndUserId(CHAMPIONSHIP_STAGE_ID, USER_ID_1) >> false
        1 * idGenerator.generate() >> CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1
        1 * championshipStageParticipantRepository.save(CHAMPIONSHIP_STAGE_PARTICIPANT_1)
        0 * _
    }

    def 'should not register participant if requester is not DRIVER'() {
        when:
        championshipStageService.registerParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should not register participant if wrong championship stage id is provided'() {
        when:
        championshipStageService.registerParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should not register the same participant'() {
        when:
        championshipStageService.registerParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * securityService.validateRequesterRoles(Role.DRIVER)
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * championshipStageParticipantRepository.existsByChampionshipStageIdAndUserId(CHAMPIONSHIP_STAGE_ID, USER_ID_1) >> true
        0 * _
    }


    // --- Participant deletion

    def 'should delete participant successfully'() {
        when:
        championshipStageService.deleteParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * championshipStageParticipantRepository.deleteByChampionshipStageIdAndUserId(CHAMPIONSHIP_STAGE_ID, USER_ID_1)
        0 * _
    }

    def 'should not delete participant if wrong championship stage id is provided'() {
        when:
        championshipStageService.deleteParticipant(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }


    // --- Participants retrieval

    def 'should provide championship stage participants'() {
        given:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> [
                ChampionshipStageParticipant.builder().userId(USER_ID_1).build(),
                ChampionshipStageParticipant.builder().userId(USER_ID_2).build()
        ]
        0 * _

        expect:
        championshipStageService.getParticipants(CHAMPIONSHIP_STAGE_ID) == [USER_ID_1, USER_ID_2] as Set
    }

    def 'should not provide participants if wrong championship stage id is provided'() {
        when:
        championshipStageService.getParticipants(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }


    // --- Judges assignment

    def 'should assign judges successfully'() {
        when:
        championshipStageService.assignJudges(CHAMPIONSHIP_STAGE_ID, [USER_ID_1, USER_ID_2])

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage.class)
        1 * userService.getUser(USER_ID_1)
        1 * userService.getUser(USER_ID_2)
        1 * championshipStageJudgeRepository.deleteAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID)
        2 * idGenerator.generate() >> CHAMPIONSHIP_STAGE_JUDGE_ID_1 >> CHAMPIONSHIP_STAGE_JUDGE_ID_2
        1 * championshipStageJudgeRepository.saveAll([CHAMPIONSHIP_STAGE_JUDGE_1, CHAMPIONSHIP_STAGE_JUDGE_2])
        0 * _
    }

    def 'should throw exception during judges assignment if championship stage is not found by provided id'() {
        when:
        championshipStageService.assignJudges(CHAMPIONSHIP_STAGE_ID, [USER_ID_1, USER_ID_2])

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should throw exception during judges assignment if requester is not owner'() {
        when:
        championshipStageService.assignJudges(CHAMPIONSHIP_STAGE_ID, [USER_ID_1, USER_ID_2])

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage.class) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should throw exception during judges assignment in case of wrong number of judges'() {
        when:
        championshipStageService.assignJudges(CHAMPIONSHIP_STAGE_ID, [USER_ID_1, USER_ID_2, USER_ID_1, USER_ID_2])

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage.class)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong number of judges'
    }

    def 'should throw exception during judges assignment in case of wrong user id'() {
        when:
        championshipStageService.assignJudges(CHAMPIONSHIP_STAGE_ID, [USER_ID_1, USER_ID_2])

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage.class)
        1 * userService.getUser(USER_ID_1)
        1 * userService.getUser(USER_ID_2) >> { throw new IllegalArgumentException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Judges retrieval

    def 'should provide championship stage judges'() {
        given:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * championshipStageJudgeRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> [
                ChampionshipStageJudge.builder().userId(USER_ID_1).build(),
                ChampionshipStageJudge.builder().userId(USER_ID_2).build()
        ]
        0 * _

        expect:
        championshipStageService.getJudges(CHAMPIONSHIP_STAGE_ID) == [USER_ID_1, USER_ID_2] as Set
    }

    def 'should not provide judges if wrong championship stage id is provided'() {
        when:
        championshipStageService.getJudges(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }


    // --- QUALIFICATION FLOW


    // --- Qualification start

    def 'should start qualification successfully'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * championshipStageJudgeRepository.countByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> 3L
        1 * championshipStageParticipantRepository.countByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> 3L
        1 * championshipStageJudgeRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> [
                CHAMPIONSHIP_STAGE_JUDGE_1,
                CHAMPIONSHIP_STAGE_JUDGE_2,
                CHAMPIONSHIP_STAGE_JUDGE_3
        ]
        1 * championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> [
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ]
        1 * championshipStageParticipantRepository.saveAll([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_3.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        ])
        1 * championshipStageRepository.save(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        0 * _
    }

    def 'should not start qualification if wrong championship stage id is provided'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship stage id'
    }

    def 'should not start qualification if requester is not owner'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage.class) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }

    def 'should not start qualification if it is not in CREATION phase'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.PAIRS_RACES).build())
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Qualification can be started from CREATION phase only'
    }

    def 'should not start qualification if judges are not assigned'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * championshipStageJudgeRepository.countByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> 0L
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Judges are not assigned'
    }

    def 'should not start qualification if no participants registered'() {
        when:
        championshipStageService.startQualification(CHAMPIONSHIP_STAGE_ID)

        then:
        1 * championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID) >> Optional.of(CHAMPIONSHIP_STAGE)
        1 * securityService.validateOwner(USER_ID_1, ChampionshipStage)
        1 * championshipStageJudgeRepository.countByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> 3L
        1 * championshipStageParticipantRepository.countByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) >> 0L
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'No participants'
    }
}
