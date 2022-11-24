package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.Championship
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.CHAMPIONSHIP_ID

class ChampionshipRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        championshipRepository.saveAndFlush(Championship.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = ['ownerId', 'organisationId', 'discipline', 'scoringSystemId']

        when:
        championshipRepository.saveAndFlush(Championship.builder().id(CHAMPIONSHIP_ID).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }
}
