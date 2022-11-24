package drift.service

import drift.model.ImageCategory
import drift.model.Organisation
import drift.repository.OrganisationRepository
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static drift.util.TestConstants.*

class OrganisationServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private organisationRepository = Mock(OrganisationRepository)
    private fileService = Mock(FileService)
    private image = Mock(MultipartFile)

    private organisationService = new OrganisationService(idGenerator, securityService, organisationRepository, fileService)


    // --- Organisation registration

    def 'should register organisation successfully'() {
        when:
        def organisationId = organisationService.registerOrganisation(ORGANISATION_REGISTRATION_DTO)

        then:
        1 * idGenerator.generate() >> ORGANISATION_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * organisationRepository.save(ORGANISATION) >> ORGANISATION
        0 * _

        and:
        organisationId == ORGANISATION_ID
    }


    // --- Organisation update

    def 'should update organisation successfully'() {
        given:
        def updatedOrganisation = ORGANISATION.toBuilder().description(DESCRIPTION_2).build()

        when:
        organisationService.updateOrganisation(ORGANISATION_ID, ORGANISATION_UPDATE_DTO)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation)
        1 * organisationRepository.save(updatedOrganisation)
        0 * _
    }

    def 'should throw exception during organisation update if organisation is not found by provided id'() {
        when:
        organisationService.updateOrganisation(ORGANISATION_ID, ORGANISATION_UPDATE_DTO)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong organisation id'
    }

    def 'should throw exception during organisation update if requester is not owner'() {
        when:
        organisationService.updateOrganisation(ORGANISATION_ID, ORGANISATION_UPDATE_DTO)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Organisation deactivation

    def 'should deactivate organisation successfully'() {
        given:
        def deactivatedOrganisation = ORGANISATION.toBuilder().active(false).build()

        when:
        organisationService.deactivateOrganisation(ORGANISATION_ID)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation)
        1 * organisationRepository.save(deactivatedOrganisation)
        0 * _
    }

    def 'should throw exception during organisation deactivation if organisation is not found by provided id'() {
        when:
        organisationService.deactivateOrganisation(ORGANISATION_ID)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong organisation id'
    }

    def 'should throw exception during organisation deactivation if requester is not owner'() {
        when:
        organisationService.deactivateOrganisation(ORGANISATION_ID)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Image upload

    def 'should upload image successfully'() {
        given:
        def updatedOrganisation = ORGANISATION.toBuilder().image(PATH).build()

        when:
        organisationService.uploadImage(ORGANISATION_ID, image)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation)
        1 * fileService.uploadImage(ImageCategory.ORGANISATION, ORGANISATION_ID, image) >> PATH
        1 * organisationRepository.save(updatedOrganisation)
        0 * _
    }

    def 'should throw exception during image upload if organisation is not found by provided id'() {
        when:
        organisationService.uploadImage(ORGANISATION_ID, image)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong organisation id'
    }

    def 'should throw exception during image upload if requester is not owner'() {
        when:
        organisationService.uploadImage(ORGANISATION_ID, image)

        then:
        1 * organisationRepository.findById(ORGANISATION_ID) >> Optional.of(ORGANISATION)
        1 * securityService.validateOwner(USER_ID_1, Organisation) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }
}
