package drift.functional

import drift.model.User

import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK

class AuthControllerFunctionalTest extends BaseFunctionalTest {

    private static final USER_REGISTRATION_URL = "$BASE_AUTH_API_URL/registration/step-1"
    private static final USER_ACTIVATION_URL = "$BASE_AUTH_API_URL/registration/step-2"
    private static final SIGN_IN_URL = "$BASE_AUTH_API_URL/sign-in"
    private static final RESET_PASSWORD_URL = "$BASE_AUTH_API_URL/reset-password"
    private static final USER_REGISTRATION_REQUEST_BODY = """
        {
            "email": "$EMAIL",
            "password": "$PASSWORD_1"
        }
    """
    private static final USER_ACTIVATION_REQUEST_BODY = """
        {
            "email": "$EMAIL",
            "verificationCode": "$VERIFICATION_CODE"
        }
    """
    private static final SIGN_IN_REQUEST_BODY = """
        {
            "email": "$EMAIL",
            "password": "$PASSWORD_1"
        }
    """
    private static final RESET_PASSWORD_REQUEST_BODY = """
        {
            "email": "$EMAIL"
        }
    """


    // --- User registration

    def 'should register user successfully'() {
        given:
        assert userRepository.findAll().isEmpty()

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_REGISTRATION_REQUEST_BODY)
                .when()
                .post(USER_REGISTRATION_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def registeredUser = userRepository.findByEmail(NORMALIZED_EMAIL).get()
        registeredUser.id.length() == 8
        registeredUser.email == NORMALIZED_EMAIL
        securityService.matches(PASSWORD_1, registeredUser.password)
        registeredUser.verificationCode.length() == 4
        !registeredUser.active
    }

    def 'should re-register deactivated user with the same email'() {
        given:
        def deactivatedUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_2_ENCODED)
                .firstName(FIRST_NAME).lastName(LAST_NAME).active(false).build()
        userRepository.saveAndFlush(deactivatedUser)

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_REGISTRATION_REQUEST_BODY)
                .when()
                .post(USER_REGISTRATION_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def registeredUser = userRepository.findById(USER_ID_1).get()
        registeredUser.id == USER_ID_1
        registeredUser.email == NORMALIZED_EMAIL
        securityService.matches(PASSWORD_1, registeredUser.password)
        !registeredUser.firstName
        !registeredUser.lastName
        registeredUser.verificationCode.length() == 4
        !registeredUser.active
    }

    def 'should not register user if active user with the same email already exists'() {
        given:
        def existingActiveUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).active(true).build()
        userRepository.saveAndFlush(existingActiveUser)

        when:
        def response = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_REGISTRATION_REQUEST_BODY)
                .when()
                .post(USER_REGISTRATION_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Active user with provided email already exists.'

        and:
        userRepository.findById(USER_ID_1).get() == existingActiveUser
    }

    def 'should not register user in case of incorrect request'() {
        given:
        assert userRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{"email":"invalid-email"}')
                .when()
                .post(USER_REGISTRATION_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        userRepository.findAll().isEmpty()
    }


    // --- User activation

    def 'should activate user successfully'() {
        given:
        def registeredUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).verificationCode(VERIFICATION_CODE).active(false).build()
        def activatedUser = registeredUser.toBuilder().verificationCode(null).active(true).build()
        userRepository.saveAndFlush(registeredUser)

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_ACTIVATION_REQUEST_BODY)
                .when()
                .post(USER_ACTIVATION_URL)
                .then()
                .statusCode(SC_OK)

        then:
        userRepository.findById(USER_ID_1).get() == activatedUser
    }

    def 'should not activate user if registered and not activated user is not found'() {
        given:
        def registeredAndActivatedUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).active(true).build()
        userRepository.saveAndFlush(registeredAndActivatedUser)

        when:
        def response = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_ACTIVATION_REQUEST_BODY)
                .when()
                .post(USER_ACTIVATION_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong email.'
    }

    def 'should activate user if wrong verification code is provided'() {
        given:
        def registeredUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).verificationCode('9102').active(false).build()
        userRepository.saveAndFlush(registeredUser)

        when:
        def response = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(USER_ACTIVATION_REQUEST_BODY)
                .when()
                .post(USER_ACTIVATION_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong verification code.'

        and:
        userRepository.findById(USER_ID_1).get() == registeredUser
    }

    def 'should not activate user in case of incorrect request'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{"email":"invalid-email"}')
                .when()
                .post(USER_ACTIVATION_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
    }


    // --- Sign in

    def 'should get access token as a result of successful sign in'() {
        given:
        def user = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(securityService.encode(PASSWORD_1)).active(true).build()
        userRepository.saveAndFlush(user)

        when:
        def accessToken = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(SIGN_IN_REQUEST_BODY)
                .when()
                .post(SIGN_IN_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        accessToken == 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDEzNjAyMjUsInVzZXJJZCI6InVzZXItaWQtMSJ9.JnybNttsyUb6hxM9DLoFIT3BO4JLEAly1Kv1Ypaq8e8'
    }

    def 'should not get access token if wrong email or password is provided'() {
        given:
        def user = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(securityService.encode(PASSWORD_2)).active(true).build()
        userRepository.saveAndFlush(user)

        when:
        def response = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(SIGN_IN_REQUEST_BODY)
                .when()
                .post(SIGN_IN_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong email or password.'
    }

    def 'should not get access token in case of incorrect request'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{"email":"invalid-email"}')
                .when()
                .post(SIGN_IN_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
    }


    // --- Reset password

    def 'should reset password successfully'() {
        given:
        def previousPassword = securityService.encode(PASSWORD_1)
        def user = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(previousPassword).active(true).build()
        userRepository.saveAndFlush(user)

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(RESET_PASSWORD_REQUEST_BODY)
                .when()
                .post(RESET_PASSWORD_URL)
                .then()
                .statusCode(SC_OK)

        and:
        user = userRepository.findById(USER_ID_1).get()

        then:
        !user.password.isBlank()
        user.password != previousPassword
    }

    def 'should not reset password if active user is not found by provided email'() {
        given:
        def user = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(securityService.encode(PASSWORD_1)).active(false).build()
        userRepository.saveAndFlush(user)

        when:
        def response = given()
                .contentType(JSON_CONTENT_TYPE)
                .body(RESET_PASSWORD_REQUEST_BODY)
                .when()
                .post(RESET_PASSWORD_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong email.'
    }

    def 'should not reset password in case of incorrect request'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{"email":"invalid-email"}')
                .when()
                .post(RESET_PASSWORD_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
    }
}
