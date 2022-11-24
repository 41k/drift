package drift.functional

import spock.lang.Ignore

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class OrganisationControllerFunctionalTest extends BaseFunctionalTest {

    private static final ORGANISATION_REGISTRATION_REQUEST_BODY = """
        {
            "name": "$NAME_1",
            "description": "$DESCRIPTION_1"
        }
    """
    private static final ORGANISATION_UPDATE_REQUEST_BODY = """
        {
            "description": "$DESCRIPTION_2"
        }
    """


    // --- Organisation registration

    def 'should register organisation successfully'() {
        given:
        assert organisationRepository.findAll().isEmpty()

        when:
        def organisationId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_REGISTRATION_REQUEST_BODY)
                .when()
                .post(BASE_ORGANISATIONS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        organisationRepository.findById(organisationId).get() == ORGANISATION.toBuilder().id(organisationId).build()
    }

    def 'should not register organisation in case of incorrect request'() {
        given:
        assert organisationRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_ORGANISATIONS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        organisationRepository.findAll().isEmpty()
    }

    def 'should not register organisation in case of request without valid access token'() {
        given:
        assert organisationRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_REGISTRATION_REQUEST_BODY)
                .when()
                .post(BASE_ORGANISATIONS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        organisationRepository.findAll().isEmpty()
    }


    // --- Organisation update

    def 'should update organisation successfully'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        and:
        def updatedOrganisation = ORGANISATION.toBuilder().description(DESCRIPTION_2).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_UPDATE_REQUEST_BODY)
                .when()
                .put(ORGANISATION_URI)
                .then()
                .statusCode(SC_OK)

        then:
        organisationRepository.findById(ORGANISATION_ID).get() == updatedOrganisation
    }

    def 'should not update organisation if organisation is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_UPDATE_REQUEST_BODY)
                .when()
                .put(ORGANISATION_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong organisation id.'
    }

    def 'should not update organisation if requester is not owner'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_UPDATE_REQUEST_BODY)
                .when()
                .put(ORGANISATION_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Organisation.'

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }

    def 'should not update organisation in case of request without valid access token'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(ORGANISATION_UPDATE_REQUEST_BODY)
                .when()
                .put(ORGANISATION_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }


    // --- Organisation deactivation

    def 'should deactivate organisation successfully'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        and:
        def deactivatedOrganisation = ORGANISATION.toBuilder().active(false).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(ORGANISATION_URI)
                .then()
                .statusCode(SC_OK)

        then:
        organisationRepository.findById(ORGANISATION_ID).get() == deactivatedOrganisation
    }

    def 'should not deactivate organisation if organisation is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(ORGANISATION_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong organisation id.'
    }

    def 'should not deactivate organisation if requester is not owner'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .delete(ORGANISATION_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Organisation.'

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }

    def 'should not deactivate organisation in case of request without valid access token'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        expect:
        when()
                .delete(ORGANISATION_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }


    // --- Image upload

    @Ignore('Fails on GitHub build machine due to no directory access rights')
    def 'should upload image successfully'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        and: 'no image has been uploaded yet'
        when()
                .get(ORGANISATION_IMAGE_PATH)
                .then()
                .statusCode(SC_NOT_FOUND)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_ORGANISATION_IMAGE_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedOrganisation = organisationRepository.findById(ORGANISATION_ID).get()
        updatedOrganisation.image == ORGANISATION_IMAGE_PATH

        and: 'image has been uploaded successfully'
        when()
                .get(ORGANISATION_IMAGE_PATH)
                .then()
                .statusCode(SC_OK)
    }

    def 'should not upload image if uploaded file is not an image'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_ORGANISATION_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: File is not an image.'

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }

    def 'should not upload image if requester is not owner of organisation'() {
        given:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_ORGANISATION_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Organisation.'

        and:
        organisationRepository.findById(ORGANISATION_ID).get() == ORGANISATION
    }

    def 'should not upload image in case of request without valid access token'() {
        expect:
        given()
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_ORGANISATION_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
