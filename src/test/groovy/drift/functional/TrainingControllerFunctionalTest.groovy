package drift.functional

import drift.model.TrainingParticipant
import spock.lang.Ignore

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class TrainingControllerFunctionalTest extends BaseFunctionalTest {

    private static final TRAINING_CREATION_REQUEST_BODY = """
        {
            "organisationId": "$ORGANISATION_ID",
            "discipline": "$DISCIPLINE",
            "name": "$NAME_1",
            "startTimestamp": $TIMESTAMP_1,
            "location": "$LOCATION_1",
            "participationInfo": "$PARTICIPATION_INFO_1"
        }
    """
    private static final TRAINING_UPDATE_REQUEST_BODY = """
        {
            "name": "$NAME_2",
            "startTimestamp": $TIMESTAMP_2,
            "location": "$LOCATION_2",
            "participationInfo": "$PARTICIPATION_INFO_2"
        }
    """


    // --- Training creation

    def 'should create training successfully'() {
        given:
        assert trainingRepository.findAll().isEmpty()

        and:
        organisationRepository.saveAndFlush(ORGANISATION)

        when:
        def trainingId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_TRAININGS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        trainingRepository.findById(trainingId).get() == TRAINING.toBuilder().id(trainingId).build()
    }

    def 'should not create training if organisation id is wrong'() {
        given:
        assert trainingRepository.findAll().isEmpty()

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_TRAININGS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong organisation id.'

        and:
        trainingRepository.findAll().isEmpty()
    }

    def 'should not create training if requester is not owner of provided organisation'() {
        given:
        assert trainingRepository.findAll().isEmpty()

        and:
        organisationRepository.saveAndFlush(ORGANISATION.toBuilder().ownerId(USER_ID_2).build())

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_TRAININGS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Organisation.'

        and:
        trainingRepository.findAll().isEmpty()
    }

    def 'should not create training in case of incorrect request'() {
        given:
        assert trainingRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_TRAININGS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        trainingRepository.findAll().isEmpty()
    }

    def 'should not create training in case of request without valid access token'() {
        given:
        assert trainingRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_TRAININGS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingRepository.findAll().isEmpty()
    }


    // --- Training update

    def 'should update training successfully'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        and:
        def updatedTraining = TRAINING.toBuilder()
                .name(NAME_2).startTimestamp(TIMESTAMP_2_AS_INSTANT)
                .location(LOCATION_2).participationInfo(PARTICIPATION_INFO_2).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_UPDATE_REQUEST_BODY)
                .when()
                .put(TRAINING_URI)
                .then()
                .statusCode(SC_OK)

        then:
        trainingRepository.findById(TRAINING_ID).get() == updatedTraining
    }

    def 'should not update training if training is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_UPDATE_REQUEST_BODY)
                .when()
                .put(TRAINING_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong training id.'
    }

    def 'should not update training if requester is not owner'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_UPDATE_REQUEST_BODY)
                .when()
                .put(TRAINING_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Training.'

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }

    def 'should not update training in case of request without valid access token'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(TRAINING_UPDATE_REQUEST_BODY)
                .when()
                .put(TRAINING_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }


    // --- Training deactivation

    def 'should deactivate training successfully'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        and:
        def deactivatedTraining = TRAINING.toBuilder().active(false).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(TRAINING_URI)
                .then()
                .statusCode(SC_OK)

        then:
        trainingRepository.findById(TRAINING_ID).get() == deactivatedTraining
    }

    def 'should not deactivate training if training is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(TRAINING_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong training id.'
    }

    def 'should not deactivate training if requester is not owner'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .delete(TRAINING_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Training.'

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }

    def 'should not deactivate training in case of request without valid access token'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        expect:
        when()
                .delete(TRAINING_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }


    // --- Placard image upload

    @Ignore('Fails on GitHub build machine due to no directory access rights')
    def 'should upload placard image successfully'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        and: 'no image has been uploaded yet'
        when()
                .get(TRAINING_IMAGE_PATH)
                .then()
                .statusCode(SC_NOT_FOUND)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_TRAINING_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedTraining = trainingRepository.findById(TRAINING_ID).get()
        updatedTraining.placardImage == TRAINING_IMAGE_PATH

        and: 'image has been uploaded successfully'
        when()
                .get(TRAINING_IMAGE_PATH)
                .then()
                .statusCode(SC_OK)
    }

    def 'should not upload placard image if uploaded file is not an image'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_TRAINING_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: File is not an image.'

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }

    def 'should not upload placard image if requester is not owner of training'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_TRAINING_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of Training.'

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }

    def 'should not upload placard image in case of request without valid access token'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)

        expect:
        given()
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_TRAINING_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingRepository.findById(TRAINING_ID).get() == TRAINING
    }


    // --- Participant registration

    def 'should register requester as participant successfully'() {
        given:
        assert trainingParticipantRepository.findAll().isEmpty()

        and:
        carRepository.saveAndFlush(CAR)
        trainingRepository.saveAndFlush(TRAINING)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def participants = trainingParticipantRepository.findAll()
        participants.size() == 1
        participants.get(0).trainingId == TRAINING_ID
        participants.get(0).userId == USER_ID_1
    }

    def 'should not register requester as participant if requester is not DRIVER'() {
        given:
        assert trainingParticipantRepository.findAll().isEmpty()

        and:
        trainingRepository.saveAndFlush(TRAINING)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Roles [DRIVER] are required.'

        and:
        trainingParticipantRepository.findAll().isEmpty()
    }

    def 'should not register requester as participant if wrong training id is provided'() {
        given:
        assert trainingParticipantRepository.findAll().isEmpty()

        and:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong training id.'

        and:
        trainingParticipantRepository.findAll().isEmpty()
    }

    def 'should not register the same participant'() {
        given:
        carRepository.saveAndFlush(CAR)
        trainingRepository.saveAndFlush(TRAINING)
        trainingParticipantRepository.saveAndFlush(TRAINING_PARTICIPANT)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def participants = trainingParticipantRepository.findAll()
        participants.size() == 1
        participants.get(0) == TRAINING_PARTICIPANT
    }

    def 'should not register requester as participant in case of request without valid access token'() {
        given:
        assert trainingParticipantRepository.findAll().isEmpty()

        expect:
        when()
                .post(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingParticipantRepository.findAll().isEmpty()
    }


    // --- Participant deletion

    def 'should delete participant successfully'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)
        trainingParticipantRepository.saveAndFlush(TRAINING_PARTICIPANT)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        trainingParticipantRepository.findAll().isEmpty()
    }

    def 'should not delete participant if wrong training id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong training id.'
    }

    def 'should not delete participant in case of request without valid access token'() {
        given:
        trainingParticipantRepository.saveAndFlush(TRAINING_PARTICIPANT)

        expect:
        when()
                .delete(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        trainingParticipantRepository.findById(TRAINING_PARTICIPANT_ID).isPresent()
    }


    // --- Participants retrieval

    def 'should provide training participants'() {
        given:
        trainingRepository.saveAndFlush(TRAINING)
        trainingParticipantRepository.saveAllAndFlush([
                TrainingParticipant.builder().id('1').trainingId(TRAINING_ID).userId(USER_ID_1).build(),
                TrainingParticipant.builder().id('2').trainingId(TRAINING_ID).userId(USER_ID_2).build()
        ])

        when:
        def participants = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(String[])

        then:
        (participants as List).sort() == [USER_ID_1, USER_ID_2]
    }

    def 'should not provide training participants if wrong training id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong training id.'
    }

    def 'should not provide training participants in case of request without valid access token'() {
        expect:
        when()
                .get(TRAINING_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
