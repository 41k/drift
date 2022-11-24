package drift.functional

import drift.dto.UserDto
import drift.model.User
import spock.lang.Ignore

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class MeControllerFunctionalTest extends BaseFunctionalTest {

    private static final USER = User.builder().id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).active(true).build()
    private static final USER_UPDATE_REQUEST_BODY = """
        {
            "password": "$PASSWORD_2",
            "firstName": "$FIRST_NAME",
            "lastName": "$LAST_NAME",
            "countryCode": "$COUNTRY_CODE",
            "city": "$CITY"
        }
    """

    // --- Requester's user retrieval

    def "should retrieve requester's user"() {
        given:
        def userDto = UserDto.from(USER)
        userRepository.saveAndFlush(USER)

        when:
        def retrievedUserDto = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(ME_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract()
                .body()
                .as(UserDto)

        then:
        retrievedUserDto == userDto
    }

    def "should not retrieve requester's user if active user is not found by id"() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(ME_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong user id.'
    }

    def "should not retrieve requester's user in case of request without valid access token"() {
        expect:
        when()
                .get(ME_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Requester's user update

    def "should update requester's user successfully"() {
        given:
        userRepository.saveAndFlush(USER)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_UPDATE_REQUEST_BODY)
                .when()
                .put(ME_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedUser = userRepository.findById(USER_ID_1).get()
        updatedUser.id == USER_ID_1
        updatedUser.email == NORMALIZED_EMAIL
        securityService.matches(PASSWORD_2, updatedUser.password)
        updatedUser.firstName == FIRST_NAME
        updatedUser.lastName == LAST_NAME
        updatedUser.countryCode == COUNTRY_CODE
        updatedUser.city == CITY
        updatedUser.active
    }

    def "should not update requester's user if active user is not found by id"() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_UPDATE_REQUEST_BODY)
                .when()
                .put(ME_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong user id.'
    }

    def "should not update requester's user in case of request without valid access token"() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_UPDATE_REQUEST_BODY)
                .when()
                .put(ME_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Requester's user deactivation

    def "should deactivate requester's user successfully"() {
        given:
        def deactivatedUser = USER.toBuilder().active(false).build()
        userRepository.saveAndFlush(USER)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(ME_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        userRepository.findById(USER_ID_1).get() == deactivatedUser
    }

    def "should not deactivate requester's user if active user is not found by id"() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(ME_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong user id.'
    }

    def "should not deactivate requester's user in case of request without valid access token"() {
        expect:
        when()
                .delete(ME_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Image upload

    @Ignore('Fails on GitHub build machine due to no directory access rights')
    def 'should upload image successfully'() {
        given:
        userRepository.saveAndFlush(USER)

        and: 'no image has been uploaded yet'
        when()
                .get(USER_IMAGE_PATH)
                .then()
                .statusCode(SC_NOT_FOUND)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_MY_IMAGE_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedUser = userRepository.findById(USER_ID_1).get()
        updatedUser.image == USER_IMAGE_PATH

        and: 'image has been uploaded successfully'
        when()
                .get(USER_IMAGE_PATH)
                .then()
                .statusCode(SC_OK)
    }

    def 'should not upload image if uploaded file is not an image'() {
        given:
        userRepository.saveAndFlush(USER)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_MY_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: File is not an image.'

        and:
        userRepository.findById(USER_ID_1).get() == USER
    }

    def 'should not upload image in case of request without valid access token'() {
        expect:
        given()
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_MY_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
