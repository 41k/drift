package drift.service

import drift.repository.ScoringSystemRepository
import spock.lang.Specification

import static drift.util.TestConstants.*

class ScoringSystemServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private scoringSystemRepository = Mock(ScoringSystemRepository)

    private scoringSystemService = new ScoringSystemService(idGenerator, securityService, scoringSystemRepository)


    // --- Scoring system creation

    def 'should create scoring system successfully'() {
        when:
        def scoringSystemId = scoringSystemService.createScoringSystem(SCORING_SYSTEM_CREATION_DTO)

        then:
        1 * idGenerator.generate() >> SCORING_SYSTEM_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * scoringSystemRepository.save(SCORING_SYSTEM) >> SCORING_SYSTEM
        0 * _

        and:
        scoringSystemId == SCORING_SYSTEM_ID
    }


    // --- Scoring system retrieval

    def 'should retrieve scoring systems successfully'() {
        given:
        1 * scoringSystemRepository.findAllByActive(true)>> [
                SCORING_SYSTEM.toBuilder().id('1').build(),
                SCORING_SYSTEM.toBuilder().id('2').build()
        ]
        0 * _

        expect:
        scoringSystemService.getScoringSystems() == [
                SCORING_SYSTEM_DTO.toBuilder().id('1').build(),
                SCORING_SYSTEM_DTO.toBuilder().id('2').build()
        ]
    }

    def 'should retrieve scoring system successfully'() {
        given:
        1 * scoringSystemRepository.findById(SCORING_SYSTEM_ID) >> Optional.of(SCORING_SYSTEM)
        0 * _

        expect:
        scoringSystemService.getScoringSystem(SCORING_SYSTEM_ID) == SCORING_SYSTEM_DTO
    }

    def 'should throw exception during scoring system retrieval if scoring system is not found by provided id'() {
        when:
        scoringSystemService.getScoringSystem(SCORING_SYSTEM_ID)

        then:
        1 * scoringSystemRepository.findById(SCORING_SYSTEM_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong scoring system id'
    }
}
