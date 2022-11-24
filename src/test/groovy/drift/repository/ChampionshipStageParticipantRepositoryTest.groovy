package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.ChampionshipStageParticipant
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.CHAMPIONSHIP_STAGE_PARTICIPANT_1
import static drift.util.TestConstants.CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1
import static drift.util.TestConstants.QUALIFICATION_RESULTS

class ChampionshipStageParticipantRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        championshipStageParticipantRepository.saveAndFlush(ChampionshipStageParticipant.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = ['championshipStageId', 'userId']

        when:
        championshipStageParticipantRepository.saveAndFlush(ChampionshipStageParticipant.builder().id(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }

    def 'should save qualification results correctly'() {
        when:
        championshipStageParticipantRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build())

        then:
        championshipStageParticipantRepository.findById(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1).get().qualificationResults == QUALIFICATION_RESULTS
    }
}
