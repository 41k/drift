package drift.functional

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class ChampionshipControllerFunctionalTest extends BaseFunctionalTest {

    private static final CHAMPIONSHIP_CREATION_REQUEST_BODY = """
        {
            "organisationId": "$ORGANISATION_ID",
            "discipline": "$DISCIPLINE",
            "scoringSystemId": "$SCORING_SYSTEM_ID"
        }
    """


    // --- Championship creation

    def 'should create championship successfully'() {
        given:
        assert championshipRepository.findAll().isEmpty()

        and:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def championshipId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIPS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        championshipRepository.findById(championshipId).get() == CHAMPIONSHIP.toBuilder().id(championshipId).build()
    }

    def 'should not create championship if organisation id is wrong'() {
        given:
        assert championshipRepository.findAll().isEmpty()

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIPS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong organisation id.'

        and:
        championshipRepository.findAll().isEmpty()
    }

    def 'should not create championship if requester is not owner of provided organisation'() {
        given:
        assert championshipRepository.findAll().isEmpty()

        and:
        organisationRepository.saveAndFlush(ORGANISATION.toBuilder().ownerId(USER_ID_2).build())

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIPS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Organisation.'

        and:
        championshipRepository.findAll().isEmpty()
    }

    def 'should not create championship in case of incorrect request'() {
        given:
        assert championshipRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_CHAMPIONSHIPS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        championshipRepository.findAll().isEmpty()
    }

    def 'should not create championship in case of request without valid access token'() {
        given:
        assert championshipRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIPS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipRepository.findAll().isEmpty()
    }


    // --- Championship deactivation

    def 'should deactivate championship successfully'() {
        given:
        championshipRepository.saveAndFlush(CHAMPIONSHIP)

        and:
        def deactivatedChampionship = CHAMPIONSHIP.toBuilder().active(false).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_URI)
                .then()
                .statusCode(SC_OK)

        then:
        championshipRepository.findById(CHAMPIONSHIP_ID).get() == deactivatedChampionship
    }

    def 'should not deactivate championship if championship is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship id.'
    }

    def 'should not deactivate championship if requester is not owner'() {
        given:
        championshipRepository.saveAndFlush(CHAMPIONSHIP)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .delete(CHAMPIONSHIP_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Championship.'

        and:
        championshipRepository.findById(CHAMPIONSHIP_ID).get() == CHAMPIONSHIP
    }

    def 'should not deactivate championship in case of request without valid access token'() {
        expect:
        when()
                .delete(CHAMPIONSHIP_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
