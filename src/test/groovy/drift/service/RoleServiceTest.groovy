package drift.service

import drift.repository.CarRepository
import drift.repository.OrganisationRepository
import spock.lang.Specification
import spock.lang.Unroll

import static drift.model.Role.*
import static drift.util.TestConstants.USER_ID_1

class RoleServiceTest extends Specification {

    private carRepository = Mock(CarRepository)
    private organisationRepository = Mock(OrganisationRepository)
    private roleService = new RoleService(carRepository, organisationRepository)

    @Unroll
    def 'should provide roles correctly when user [#description]'() {
        given:
        1 * carRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> hasCar
        1 * organisationRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> hasOrganisation
        0 * _

        expect:
        roleService.getRoles(USER_ID_1) == roles

        where:
        description                      | hasCar | hasOrganisation || roles
        'does not have anything'         | false  | false           || [SPECTATOR]
        'has car(s)'                     | true   | false           || [SPECTATOR, DRIVER]
        'has organisation(s)'            | false  | true            || [SPECTATOR, ORGANIZER]
        'has car(s) and organisation(s)' | true   | true            || [SPECTATOR, DRIVER, ORGANIZER]
    }

    def 'should check if user has roles'() {
        when:
        def result = roleService.userHasRoles(USER_ID_1, [ORGANIZER, SPECTATOR])

        then:
        1 * carRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> true
        1 * organisationRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> true
        0 * _

        and:
        result

        when:
        result = roleService.userHasRoles(USER_ID_1, [DRIVER, ORGANIZER])

        then:
        1 * carRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> false
        1 * organisationRepository.existsByOwnerIdAndActive(USER_ID_1, true) >> false
        0 * _

        and:
        !result
    }
}
