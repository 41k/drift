package drift.service

import drift.model.Championship
import drift.repository.ChampionshipRepository
import spock.lang.Specification

import static drift.util.TestConstants.*

class ChampionshipServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private organisationService = Mock(OrganisationService)
    private championshipRepository = Mock(ChampionshipRepository)

    private championshipService = new ChampionshipService(idGenerator, securityService, organisationService, championshipRepository)


    // --- Championship creation

    def 'should create championship successfully'() {
        when:
        def championshipId = championshipService.createChampionship(CHAMPIONSHIP_CREATION_DTO)

        then:
        1 * organisationService.getRequesterOrganisation(ORGANISATION_ID)
        1 * idGenerator.generate() >> CHAMPIONSHIP_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * championshipRepository.save(CHAMPIONSHIP) >> CHAMPIONSHIP
        0 * _

        and:
        championshipId == CHAMPIONSHIP_ID
    }

    def 'should not create championship if wrong organisation id is provided'() {
        when:
        championshipService.createChampionship(CHAMPIONSHIP_CREATION_DTO)

        then:
        1 * organisationService.getRequesterOrganisation(ORGANISATION_ID) >> { throw new IllegalArgumentException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Championship deactivation

    def 'should deactivate championship successfully'() {
        given:
        def deactivatedChampionship = CHAMPIONSHIP.toBuilder().active(false).build()

        when:
        championshipService.deactivateChampionship(CHAMPIONSHIP_ID)

        then:
        1 * championshipRepository.findById(CHAMPIONSHIP_ID) >> Optional.of(CHAMPIONSHIP)
        1 * securityService.validateOwner(USER_ID_1, Championship)
        1 * championshipRepository.save(deactivatedChampionship)
        0 * _
    }

    def 'should throw exception during championship deactivation if championship is not found by provided id'() {
        when:
        championshipService.deactivateChampionship(CHAMPIONSHIP_ID)

        then:
        1 * championshipRepository.findById(CHAMPIONSHIP_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong championship id'
    }

    def 'should throw exception during championship deactivation if requester is not owner'() {
        when:
        championshipService.deactivateChampionship(CHAMPIONSHIP_ID)

        then:
        1 * championshipRepository.findById(CHAMPIONSHIP_ID) >> Optional.of(CHAMPIONSHIP)
        1 * securityService.validateOwner(USER_ID_1, Championship) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }
}
