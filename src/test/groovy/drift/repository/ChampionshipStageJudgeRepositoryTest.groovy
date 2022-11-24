package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.ChampionshipStageJudge
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.CHAMPIONSHIP_STAGE_JUDGE_ID_1

class ChampionshipStageJudgeRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        championshipStageJudgeRepository.saveAndFlush(ChampionshipStageJudge.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = ['championshipStageId', 'userId']

        when:
        championshipStageJudgeRepository.saveAndFlush(ChampionshipStageJudge.builder().id(CHAMPIONSHIP_STAGE_JUDGE_ID_1).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }
}
