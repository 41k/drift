package drift.functional

import drift.model.Role
import drift.model.User

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED

class RoleControllerFunctionalTest extends BaseFunctionalTest {

    // --- User's roles retrieval

    def "should retrieve user's roles"() {
        given:
        def user = User.builder().id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).active(true).build()
        userRepository.saveAndFlush(user)

        when:
        def retrievedRoles = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(ROLES_URI)
                .then()
                .statusCode(SC_OK)
                .extract()
                .body()
                .as(String[])

        then:
        retrievedRoles == [Role.SPECTATOR.name()]
    }

    def "should not retrieve user's roles in case of request without valid access token"() {
        expect:
        when()
                .get(ROLES_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
