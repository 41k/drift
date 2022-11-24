package drift.functional

import spock.lang.Ignore

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class CarControllerFunctionalTest extends BaseFunctionalTest {

    private static final CAR_REGISTRATION_REQUEST_BODY = """
        {
            "brand": "$BRAND",
            "model": "$MODEL",
            "power": $POWER_1
        }
    """
    private static final CAR_UPDATE_REQUEST_BODY = """
        {
            "power": $POWER_2
        }
    """


    // --- Car registration

    def 'should register car successfully'() {
        given:
        assert carRepository.findAll().isEmpty()

        when:
        def carId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_REGISTRATION_REQUEST_BODY)
                .when()
                .post(BASE_CARS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        carRepository.findById(carId).get() == CAR.toBuilder().id(carId).build()
    }

    def 'should not register car in case of incorrect request'() {
        given:
        assert carRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_CARS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        carRepository.findAll().isEmpty()
    }

    def 'should not register car in case of request without valid access token'() {
        given:
        assert carRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_REGISTRATION_REQUEST_BODY)
                .when()
                .post(BASE_CARS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        carRepository.findAll().isEmpty()
    }


    // --- Car update

    def 'should update car successfully'() {
        given:
        carRepository.saveAndFlush(CAR)

        and:
        def updatedCar = CAR.toBuilder().power(POWER_2).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_UPDATE_REQUEST_BODY)
                .when()
                .put(CAR_URI)
                .then()
                .statusCode(SC_OK)

        then:
        carRepository.findById(CAR_ID).get() == updatedCar
    }

    def 'should not update car if car is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_UPDATE_REQUEST_BODY)
                .when()
                .put(CAR_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong car id.'
    }

    def 'should not update car if requester is not owner'() {
        given:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_UPDATE_REQUEST_BODY)
                .when()
                .put(CAR_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Car.'

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }

    def 'should not update car in case of request without valid access token'() {
        given:
        carRepository.saveAndFlush(CAR)

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CAR_UPDATE_REQUEST_BODY)
                .when()
                .put(CAR_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }


    // --- Car deactivation

    def 'should deactivate car successfully'() {
        given:
        carRepository.saveAndFlush(CAR)

        and:
        def deactivatedCar = CAR.toBuilder().active(false).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CAR_URI)
                .then()
                .statusCode(SC_OK)

        then:
        carRepository.findById(CAR_ID).get() == deactivatedCar
    }

    def 'should not deactivate car if car is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CAR_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong car id.'
    }

    def 'should not deactivate car if requester is not owner'() {
        given:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .delete(CAR_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Car.'

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }

    def 'should not deactivate car in case of request without valid access token'() {
        given:
        carRepository.saveAndFlush(CAR)

        expect:
        when()
                .delete(CAR_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }


    // --- Image upload

    @Ignore('Fails on GitHub build machine due to no directory access rights')
    def 'should upload image successfully'() {
        given:
        carRepository.saveAndFlush(CAR)

        and: 'no image has been uploaded yet'
        when()
                .get(CAR_IMAGE_PATH)
                .then()
                .statusCode(SC_NOT_FOUND)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CAR_IMAGE_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedCar = carRepository.findById(CAR_ID).get()
        updatedCar.image == CAR_IMAGE_PATH

        and: 'image has been uploaded successfully'
        when()
                .get(CAR_IMAGE_PATH)
                .then()
                .statusCode(SC_OK)
    }

    def 'should not upload image if uploaded file is not an image'() {
        given:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_CAR_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: File is not an image.'

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }

    def 'should not upload image if requester is not car owner'() {
        given:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CAR_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Car.'

        and:
        carRepository.findById(CAR_ID).get() == CAR
    }

    def 'should not upload image in case of request without valid access token'() {
        expect:
        given()
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CAR_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
