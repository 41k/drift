package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.Training
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.TRAINING_ID

class TrainingRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        trainingRepository.saveAndFlush(Training.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = [
                'ownerId', 'organisationId', 'name', 'startTimestamp', 'location', 'participationInfo', 'discipline'
        ]

        when:
        trainingRepository.saveAndFlush(Training.builder().id(TRAINING_ID).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }
}
