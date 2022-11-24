package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.TrainingParticipant
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.TRAINING_PARTICIPANT_ID

class TrainingParticipantRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        trainingParticipantRepository.saveAndFlush(TrainingParticipant.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = ['trainingId', 'userId']

        when:
        trainingParticipantRepository.saveAndFlush(TrainingParticipant.builder().id(TRAINING_PARTICIPANT_ID).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }
}
