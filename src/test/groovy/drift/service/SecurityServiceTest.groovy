package drift.service

import drift.configuration.properties.SecurityProperties
import drift.model.Car
import drift.model.Role
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Unroll

import static drift.util.TestConstants.*

class SecurityServiceTest extends Specification {

    private static final ACCESS_TOKEN = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDEyNzM4ODUsInVzZXJJZCI6InVzZXItaWQtMSJ9.RgfBNQT2dNQX6D64Id7eUwfjNT7cniKHafZO25stnFA'

    private properties = new SecurityProperties(
            salt: 'salt',
            tokenKey: 'token-key',
            tokenTtlInMillis: 60000
    )
    private roleService = Mock(RoleService)
    private securityService = new SecurityService(properties, roleService, CLOCK)


    def 'should encode value and check if encoded value matches original value'() {
        given:
        def value = 'value'

        when:
        def encodedValue = securityService.encode(value)

        then:
        securityService.matches(value, encodedValue)
    }

    def 'should generate verification code as 4 digit sequence'() {
        given:
        def codePattern = ~/\d{4}/

        when:
        def code = securityService.generateVerificationCode()

        then:
        codePattern.matcher(code).matches()
    }

    def 'should generate access token'() {
        expect:
        securityService.generateAccessToken(USER_ID_1) == ACCESS_TOKEN
    }

    def 'should setup security context successfully'() {
        when:
        securityService.setupSecurityContext(ACCESS_TOKEN)

        then:
        SecurityContextHolder.getContext().getAuthentication()
        securityService.getRequesterId() == USER_ID_1
    }

    @Unroll
    def 'should not setup security context if access token #incorrectness'() {
        when:
        securityService.setupSecurityContext(token)

        then:
        !SecurityContextHolder.getContext().getAuthentication()

        where:
        incorrectness              || token
        'is expired'               || 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDEyNzI4ODUsInVzZXJJZCI6InVzZXItaWQtMSJ9.SOq8bYOW2LZqsSUGwqgUOlMNEIotQRzDQOqyU32XZoQ'
        'does not contain user id' || 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDEyNzM4ODV9.iKanYH42VQcSLtKvf0mFYTzdr41tZ06K-AVwmA2fGPQ'
        'is truncated'             || 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDEyNzM4ODUsInVzZXJJZCI6InVzZXItaWQtMSJ9.RgfBNQT2dNQX6D64Id7eUwfjNT7cniKHafZO25'
        'is not JWT'               || 'abc'
    }

    def 'should validate owner successfully'() {
        given:
        securityService.setupSecurityContext(ACCESS_TOKEN)

        when:
        securityService.validateOwner(USER_ID_1, Car)

        then:
        noExceptionThrown()
    }

    def 'should throw exception if requester id and owner id do not match'() {
        given:
        securityService.setupSecurityContext(ACCESS_TOKEN)

        when:
        securityService.validateOwner(USER_ID_2, Car)

        then:
        def exception = thrown(SecurityException)
        exception.message == 'Requester should be owner of Car'
    }

    def 'should validate requester roles successfully'() {
        given:
        securityService.setupSecurityContext(ACCESS_TOKEN)

        when:
        securityService.validateRequesterRoles(Role.DRIVER, Role.ORGANIZER)

        then:
        1 * roleService.userHasRoles(USER_ID_1, [Role.DRIVER, Role.ORGANIZER]) >> true
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should throw exception if requester does not have required roles'() {
        given:
        securityService.setupSecurityContext(ACCESS_TOKEN)

        when:
        securityService.validateRequesterRoles(Role.DRIVER, Role.ORGANIZER)

        then:
        1 * roleService.userHasRoles(USER_ID_1, [Role.DRIVER, Role.ORGANIZER]) >> false
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == 'Roles [DRIVER, ORGANIZER] are required'
    }
}
