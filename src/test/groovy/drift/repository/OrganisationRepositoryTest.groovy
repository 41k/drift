package drift.repository

import drift.functional.BaseFunctionalTest
import drift.model.Organisation
import org.springframework.orm.jpa.JpaSystemException

import javax.validation.ConstraintViolationException

import static drift.util.TestConstants.*

class OrganisationRepositoryTest extends BaseFunctionalTest {

    def 'should throw exception during saving if [id] field is not set'() {
        when:
        organisationRepository.saveAndFlush(Organisation.builder().build())

        then:
        def exception = thrown(JpaSystemException)
        exception.message.contains('ids for this class must be manually assigned before calling save')
    }

    def 'should throw exception during saving if name is not unique'() {
        when:
        organisationRepository.saveAllAndFlush([
                Organisation.builder().id('1').ownerId(USER_ID_1).name(NAME_1).build(),
                Organisation.builder().id('2').ownerId(USER_ID_2).name(NAME_1).build()
        ])

        then:
        def exception = thrown(Exception)
        exception.cause.cause.message.contains("Duplicate entry '$NAME_1' for key 'name'")
    }

    def 'should throw exception during saving if required fields are not set'() {
        given:
        def requiredFields = ['ownerId', 'name']

        when:
        organisationRepository.saveAndFlush(Organisation.builder().id(ORGANISATION_ID).build())

        then:
        def exception = thrown(ConstraintViolationException)
        requiredFields.forEach({
            field -> assert exception.message.contains("interpolatedMessage='must not be null', propertyPath=$field")
        })
    }
}
