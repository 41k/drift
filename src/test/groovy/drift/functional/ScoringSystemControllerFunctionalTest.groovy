package drift.functional

import drift.dto.ScoringSystemDto

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class ScoringSystemControllerFunctionalTest extends BaseFunctionalTest {

    private static final SCORING_SYSTEM_CREATION_REQUEST_BODY = """
        {
            "name": "$NAME_1",
            "participationPoints": $PARTICIPATION_POINTS,
            "qualificationPoints": $QUALIFICATION_POINTS_AS_JSON,
            "points": $POINTS_AS_JSON,
            "participantsAfterQualification": $PARTICIPANTS_AFTER_QUALIFICATION
        }
    """

    def cleanup() {
        scoringSystemRepository.deleteAll()
        scoringSystemRepository.flush()
    }

    // --- Scoring systems retrieval

    def 'should retrieve predefined scoring systems'() {
        when:
        def retrievedScoringSystems = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(BASE_SCORING_SYSTEMS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract()
                .body()
                .as(ScoringSystemDto[])

        then:
        retrievedScoringSystems == [
                ScoringSystemDto.builder()
                        .id('1')
                        .name('Drift Masters')
                        .participationPoints(1d)
                        .qualificationPoints([7d, 6d, 5d, 4d, 4d, 4d, 4d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 2d])
                        .points([100d, 80d, 64d, 64d, 48d, 48d, 48d, 48d, 32d, 32d, 32d, 32d, 32d, 32d, 32d, 32d, 16d])
                        .participantsAfterQualification(32)
                        .build(),
                ScoringSystemDto.builder()
                        .id('2')
                        .name('Racing.by - 24')
                        .participationPoints(0d)
                        .qualificationPoints([10d, 8d, 5d, 4d, 4d, 4d, 4d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 1d])
                        .points([100d, 90d, 78d, 65d, 48d, 48d, 48d, 48d, 16d, 16d, 16d, 16d, 8d, 8d, 8d, 8d, 0d])
                        .participantsAfterQualification(24)
                        .build(),
                ScoringSystemDto.builder()
                        .id('3')
                        .name('Racing.by - 32')
                        .participationPoints(0d)
                        .qualificationPoints([10d, 8d, 5d, 4d, 4d, 4d, 4d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 3d, 1d])
                        .points([100d, 90d, 78d, 65d, 48d, 48d, 48d, 48d, 32d, 32d, 32d, 32d, 32d, 32d, 32d, 32d, 16d])
                        .participantsAfterQualification(32)
                        .build(),
                ScoringSystemDto.builder()
                        .id('4')
                        .name('FIA')
                        .participationPoints(0d)
                        .qualificationPoints([25d, 21d, 19d, 17d, 12d, 12d, 9d, 9d, 6d, 6d, 6d, 6d, 4d, 4d, 4d, 4d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 1d])
                        .points([210d, 185d, 160d, 135d, 110d, 110d, 110d, 110d, 80d, 80d, 80d, 80d, 80d, 80d, 80d, 80d, 40d])
                        .participantsAfterQualification(32)
                        .build()
        ]
    }

    def 'should not retrieve predefined scoring systems in case of request without valid access token'() {
        expect:
        when()
                .get(BASE_SCORING_SYSTEMS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }

    def 'should retrieve scoring system by id successfully'() {
        given:
        scoringSystemRepository.saveAndFlush(SCORING_SYSTEM)

        when:
        def scoringSystem = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(SCORING_SYSTEM_URI)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(ScoringSystemDto)

        then:
        scoringSystem == SCORING_SYSTEM_DTO
    }

    def 'should not retrieve scoring system by id if scoring system is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(SCORING_SYSTEM_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong scoring system id.'
    }

    def 'should not retrieve scoring system by id in case of request without valid access token'() {
        expect:
        when()
                .get(SCORING_SYSTEM_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Scoring system creation

    def 'should create scoring system successfully'() {
        given:
        assert scoringSystemRepository.findAll().isEmpty()

        when:
        def scoringSystemId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(SCORING_SYSTEM_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_SCORING_SYSTEMS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        scoringSystemRepository.findById(scoringSystemId).get() == SCORING_SYSTEM.toBuilder().id(scoringSystemId).build()
    }

    def 'should not create scoring system in case of incorrect request'() {
        given:
        assert scoringSystemRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_SCORING_SYSTEMS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        scoringSystemRepository.findAll().isEmpty()
    }

    def 'should not create scoring system in case of request without valid access token'() {
        given:
        assert scoringSystemRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(SCORING_SYSTEM_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_SCORING_SYSTEMS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        scoringSystemRepository.findAll().isEmpty()
    }
}
