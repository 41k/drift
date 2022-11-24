package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.ScoringSystem
import org.springframework.orm.jpa.JpaSystemException
import spock.lang.Unroll

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.*

class ScoringSystemRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        scoringSystemRepository.saveAndFlush(ScoringSystem.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if name is not unique'() {
        when:
        scoringSystemRepository.saveAllAndFlush([
                SCORING_SYSTEM.toBuilder().id('1').name(NAME_1).build(),
                SCORING_SYSTEM.toBuilder().id('2').name(NAME_1).build()
        ])

        then:
        def exception = thrown(Exception)
        exception.cause.cause.message.contains("Duplicate entry '$NAME_1' for key 'name'")
    }

    @Unroll
    def 'should throw exception during saving if required fields are not set'() {
        when:
        scoringSystemRepository.saveAndFlush(ScoringSystem.builder().id(SCORING_SYSTEM_ID).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='$message', propertyPath=$field")
        })

        where:
        message             | requiredFields
        'must not be null'  | ['ownerId', 'name', 'participationPoints', 'participantsAfterQualification']
        'must not be empty' | ['qualificationPoints', 'points']
    }
}
