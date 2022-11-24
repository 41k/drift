package drift.functional

import drift.model.ChampionshipStageJudge
import drift.model.ChampionshipStageParticipant
import drift.model.ChampionshipStagePhase
import drift.model.User
import spock.lang.Ignore

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.apache.http.HttpStatus.*

class ChampionshipStageControllerFunctionalTest extends BaseFunctionalTest {

    private static final CHAMPIONSHIP_STAGE_CREATION_REQUEST_BODY = """
        {
            "championshipId": "$CHAMPIONSHIP_ID",
            "name": "$NAME_1",
            "duration": $DURATION_1,
            "startTimestamp": $TIMESTAMP_1,
            "location": "$LOCATION_1",
            "description": "$DESCRIPTION_1",
            "participationInfo": "$PARTICIPATION_INFO_1",
            "attempts": $N_ATTEMPTS_1,
            "omt": $N_OMT_1
        }
    """
    private static final CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY = """
        {
            "name": "$NAME_2",
            "duration": $DURATION_2,
            "startTimestamp": $TIMESTAMP_2,
            "location": "$LOCATION_2",
            "description": "$DESCRIPTION_2",
            "participationInfo": "$PARTICIPATION_INFO_2",
            "attempts": $N_ATTEMPTS_2,
            "omt": $N_OMT_2
        }
    """
    private static final CHAMPIONSHIP_STAGE_JUDGES_ASSIGNMENT_REQUEST_BODY = """
        {
            "userIds": ["$USER_ID_1", "$USER_ID_2"]
        }
    """
    private static final UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY = """
            {
                "judgeUserId": "$CHAMPIONSHIP_STAGE_JUDGE_3.userId",
                "participantUserId": "$CHAMPIONSHIP_STAGE_PARTICIPANT_2.userId",
                "attemptsPoints": $UPDATED_ATTEMPTS_POINTS
            }
    """


    // --- Championship stage creation

    def 'should create championship stage successfully'() {
        given:
        assert championshipStageRepository.findAll().isEmpty()

        and:
        championshipRepository.saveAndFlush(CHAMPIONSHIP)

        when:
        def championshipStageId = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIP_STAGES_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().asString()

        then:
        championshipStageRepository.findById(championshipStageId).get() ==
                CHAMPIONSHIP_STAGE.toBuilder().id(championshipStageId).build()
    }

    def 'should not create championship stage in case of incorrect request'() {
        given:
        assert championshipStageRepository.findAll().isEmpty()

        expect:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post(BASE_CHAMPIONSHIP_STAGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        and:
        championshipStageRepository.findAll().isEmpty()
    }

    def 'should not create championship stage if wrong championship id is provided'() {
        given:
        assert championshipStageRepository.findAll().isEmpty()

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIP_STAGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship id.'

        and:
        championshipStageRepository.findAll().isEmpty()
    }

    def 'should not create championship stage in case of request without valid access token'() {
        given:
        assert championshipStageRepository.findAll().isEmpty()

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_CREATION_REQUEST_BODY)
                .when()
                .post(BASE_CHAMPIONSHIP_STAGES_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageRepository.findAll().isEmpty()
    }


    // --- Championship stage update

    def 'should update championship stage successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        and:
        def updatedChampionshipStage = CHAMPIONSHIP_STAGE.toBuilder()
                .name(NAME_2).duration(DURATION_2).startTimestamp(TIMESTAMP_2_AS_INSTANT)
                .location(LOCATION_2).description(DESCRIPTION_2).participationInfo(PARTICIPATION_INFO_2)
                .attempts(N_ATTEMPTS_2).omt(N_OMT_2).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY)
                .when()
                .put(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_OK)

        then:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == updatedChampionshipStage
    }

    def 'should not update championship stage if championship stage is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY)
                .when()
                .put(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not update championship stage if requester is not owner'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY)
                .when()
                .put(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of ChampionshipStage.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not update championship stage in case of request without valid access token'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY)
                .when()
                .put(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not update championship stage if it is not in CREATION phase'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_UPDATE_REQUEST_BODY)
                .when()
                .put(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Championship stage update is prohibited during QUALIFICATION phase.'
    }


    // --- Championship stage deactivation

    def 'should deactivate championship stage successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        and:
        def deactivatedChampionshipStage = CHAMPIONSHIP_STAGE.toBuilder().active(false).build()

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_OK)

        then:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == deactivatedChampionshipStage
    }

    def 'should not deactivate championship stage if championship stage is not found by id'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not deactivate championship stage if requester is not owner'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .delete(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of ChampionshipStage.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not deactivate championship stage in case of request without valid access token'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        expect:
        when()
                .delete(CHAMPIONSHIP_STAGE_URI)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }


    // --- Placard image upload

    @Ignore('Fails on GitHub build machine due to no directory access rights')
    def 'should upload placard image successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        and: 'no image has been uploaded yet'
        when()
                .get(CHAMPIONSHIP_STAGE_IMAGE_PATH)
                .then()
                .statusCode(SC_NOT_FOUND)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CHAMPIONSHIP_STAGE_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def updatedChampionshipStage = championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get()
        updatedChampionshipStage.placardImage == CHAMPIONSHIP_STAGE_IMAGE_PATH

        and: 'image has been uploaded successfully'
        when()
                .get(CHAMPIONSHIP_STAGE_IMAGE_PATH)
                .then()
                .statusCode(SC_OK)
    }

    def 'should not upload placard image if uploaded file is not an image'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_CHAMPIONSHIP_STAGE_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: File is not an image.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not upload placard image if requester is not owner of championship stage'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CHAMPIONSHIP_STAGE_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of ChampionshipStage.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not upload placard image in case of request without valid access token'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        expect:
        given()
                .multiPart('image', IMAGE)
                .when()
                .post(UPLOAD_CAR_IMAGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not upload image if championship stage is not in CREATION phase'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.PAIRS_RACES).build())

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .multiPart('image', TEXT_FILE)
                .when()
                .post(UPLOAD_CHAMPIONSHIP_STAGE_PLACARD_IMAGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Championship stage update is prohibited during PAIRS_RACES phase.'
    }


    // --- Participant registration

    def 'should register requester as participant successfully'() {
        given:
        assert championshipStageParticipantRepository.findAll().isEmpty()

        and:
        carRepository.saveAndFlush(CAR)
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def participants = championshipStageParticipantRepository.findAll()
        participants.size() == 1
        participants.get(0).championshipStageId == CHAMPIONSHIP_STAGE_ID
        participants.get(0).userId == USER_ID_1
    }

    def 'should not register requester as participant if requester is not DRIVER'() {
        given:
        assert championshipStageParticipantRepository.findAll().isEmpty()

        and:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Roles [DRIVER] are required.'

        and:
        championshipStageParticipantRepository.findAll().isEmpty()
    }

    def 'should not register requester as participant if wrong championship stage id is provided'() {
        given:
        assert championshipStageParticipantRepository.findAll().isEmpty()

        and:
        carRepository.saveAndFlush(CAR)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'

        and:
        championshipStageParticipantRepository.findAll().isEmpty()
    }

    def 'should not register the same participant'() {
        given:
        carRepository.saveAndFlush(CAR)
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAndFlush(CHAMPIONSHIP_STAGE_PARTICIPANT_1)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def participants = championshipStageParticipantRepository.findAll()
        participants.size() == 1
        participants.get(0) == CHAMPIONSHIP_STAGE_PARTICIPANT_1
    }

    def 'should not register requester as participant in case of request without valid access token'() {
        given:
        assert championshipStageParticipantRepository.findAll().isEmpty()

        expect:
        when()
                .post(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageParticipantRepository.findAll().isEmpty()
    }


    // --- Participant deletion

    def 'should delete participant successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAndFlush(CHAMPIONSHIP_STAGE_PARTICIPANT_1)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        championshipStageParticipantRepository.findAll().isEmpty()
    }

    def 'should not delete participant if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .delete(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not delete participant in case of request without valid access token'() {
        given:
        championshipStageParticipantRepository.saveAndFlush(CHAMPIONSHIP_STAGE_PARTICIPANT_1)

        expect:
        when()
                .delete(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        and:
        championshipStageParticipantRepository.findById(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1).isPresent()
    }


    // --- Participants retrieval

    def 'should provide championship stage participants'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAllAndFlush([
                ChampionshipStageParticipant.builder().id('1').championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_1).build(),
                ChampionshipStageParticipant.builder().id('2').championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_2).build()
        ])

        when:
        def participants = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(String[])

        then:
        (participants as List).sort() == [USER_ID_1, USER_ID_2]
    }

    def 'should not provide championship stage if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not provide championship stage in case of request without valid access token'() {
        expect:
        when()
                .get(CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Judges assignment

    def 'should assign judges successfully'() {
        given:
        userRepository.saveAllAndFlush([
                User.builder().id(USER_ID_1).email('email-1').password('pwd').active(true).build(),
                User.builder().id(USER_ID_2).email('email-2').password('pwd').active(true).build()
        ])
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_1.toBuilder().userId('1').build())
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        and:
        assert championshipStageJudgeRepository.findAll().size() == 1

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_JUDGES_ASSIGNMENT_REQUEST_BODY)
                .when()
                .post(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        def participants = championshipStageJudgeRepository.findAll().sort({ it.userId })
        participants.size() == 2
        participants.get(0).championshipStageId == CHAMPIONSHIP_STAGE_ID
        participants.get(0).userId == USER_ID_1
        participants.get(1).championshipStageId == CHAMPIONSHIP_STAGE_ID
        participants.get(1).userId == USER_ID_2
    }

    def 'should not assign judges if wrong championship stage id is provided'() {
        given:
        userRepository.saveAllAndFlush([
                User.builder().id(USER_ID_1).email('email-1').password('pwd').active(true).build(),
                User.builder().id(USER_ID_2).email('email-2').password('pwd').active(true).build()
        ])
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_1)

        and:
        assert championshipStageJudgeRepository.findAll().size() == 1

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_JUDGES_ASSIGNMENT_REQUEST_BODY)
                .when()
                .post(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'

        and:
        def participants = championshipStageJudgeRepository.findAll()
        participants.size() == 1
        participants.get(0) == CHAMPIONSHIP_STAGE_JUDGE_1
    }

    def 'should not assign judges if wrong user ids are provided'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_1)

        and:
        assert championshipStageJudgeRepository.findAll().size() == 1

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_JUDGES_ASSIGNMENT_REQUEST_BODY)
                .when()
                .post(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong user id.'

        and:
        def participants = championshipStageJudgeRepository.findAll()
        participants.size() == 1
        participants.get(0) == CHAMPIONSHIP_STAGE_JUDGE_1
    }

    def 'should not assign judges if wrong number of judges is provided'() {
        given:
        userRepository.saveAllAndFlush([
                User.builder().id(USER_ID_1).email('email-1').password('pwd').active(true).build(),
                User.builder().id(USER_ID_2).email('email-2').password('pwd').active(true).build()
        ])
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_1)
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        and:
        assert championshipStageJudgeRepository.findAll().size() == 1

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{"userIds":["1","2","3","4"]}')
                .when()
                .post(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong number of judges.'

        and:
        def participants = championshipStageJudgeRepository.findAll()
        participants.size() == 1
        participants.get(0) == CHAMPIONSHIP_STAGE_JUDGE_1
    }

    def 'should not assign judges in case of request without valid access token'() {
        given:
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_1)

        and:
        assert championshipStageJudgeRepository.findAll().size() == 1

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(CHAMPIONSHIP_STAGE_JUDGES_ASSIGNMENT_REQUEST_BODY)
                .when()
                .post(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        then:
        def participants = championshipStageJudgeRepository.findAll()
        participants.size() == 1
        participants.get(0) == CHAMPIONSHIP_STAGE_JUDGE_1
    }


    // --- Judges retrieval

    def 'should provide championship stage judges'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageJudgeRepository.saveAllAndFlush([
                ChampionshipStageJudge.builder().id('1').championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_1).build(),
                ChampionshipStageJudge.builder().id('2').championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_2).build()
        ])

        when:
        def participants = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(String[])

        then:
        (participants as List).sort() == [USER_ID_1, USER_ID_2]
    }

    def 'should not provide championship stage judges if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not provide championship stage judges in case of request without valid access token'() {
        expect:
        when()
                .get(CHAMPIONSHIP_STAGE_JUDGES_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- QUALIFICATION FLOW


    // --- Qualification start

    def 'should start qualification successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageJudgeRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_JUDGE_1,
                CHAMPIONSHIP_STAGE_JUDGE_2,
                CHAMPIONSHIP_STAGE_JUDGE_3
        ])
        championshipStageParticipantRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ])

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() ==
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build()

        and:
        championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) == [
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_3.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        ]
    }

    def 'should not start qualification if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not start qualification if requester is not owner'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ])

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser2)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract().body().asString()

        then:
        response == 'Unauthorized: Requester should be owner of ChampionshipStage.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE

        and:
        championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) == [
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ]
    }

    def 'should not start qualification if request without valid access token'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ])

        when:
        when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        then:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE

        and:
        championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) == [
                CHAMPIONSHIP_STAGE_PARTICIPANT_1,
                CHAMPIONSHIP_STAGE_PARTICIPANT_2,
                CHAMPIONSHIP_STAGE_PARTICIPANT_3
        ]
    }

    def 'should not start qualification if it is not in CREATION phase'() {
        given:
        def championshipStage = CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.PAIRS_RACES).build()
        def participants = [
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_3.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build()
        ]
        championshipStageRepository.saveAndFlush(championshipStage)
        championshipStageParticipantRepository.saveAllAndFlush(participants)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Qualification can be started from CREATION phase only.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == championshipStage

        and:
        championshipStageParticipantRepository.findAllByChampionshipStageId(CHAMPIONSHIP_STAGE_ID) == participants
    }

    def 'should not start qualification if judges are not assigned'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Judges are not assigned.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }

    def 'should not start qualification if no participants registered'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageJudgeRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_JUDGE_1,
                CHAMPIONSHIP_STAGE_JUDGE_2,
                CHAMPIONSHIP_STAGE_JUDGE_3
        ])

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .post(START_QUALIFICATION_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: No participants.'

        and:
        championshipStageRepository.findById(CHAMPIONSHIP_STAGE_ID).get() == CHAMPIONSHIP_STAGE
    }


    // --- Update qualification results

    def 'should update qualification results successfully'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_3)
        championshipStageParticipantRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build())

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_OK)

        then:
        championshipStageParticipantRepository.findById(CHAMPIONSHIP_STAGE_PARTICIPANT_2.id).get() ==
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(UPDATED_QUALIFICATION_RESULTS).build()
    }

    def 'should not update qualification results if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not update qualification results if request is invalid'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)

        then:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }

    def 'should not update qualification results if request without valid access token'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)

        then:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }

    def 'should not update qualification results if championship stage is not in QUALIFICATION phase'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.PAIRS_RACES).build())
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_3)
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Qualification results can be updated during QUALIFICATION phase only.'

        and:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }

    def 'should not update qualification results if wrong judge user id is provided'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong judge user id.'

        and:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }

    def 'should not update qualification results if wrong participant user id is provided'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().phase(ChampionshipStagePhase.QUALIFICATION).build())
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_3)
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong participant user id.'

        and:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }

    def 'should not update qualification results if wrong number of attempts is provided'() {
        given:
        championshipStageRepository.saveAndFlush(
                CHAMPIONSHIP_STAGE.toBuilder().attempts(2).phase(ChampionshipStagePhase.QUALIFICATION).build())
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_3)
        def participant = CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        championshipStageParticipantRepository.saveAndFlush(participant)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(UPDATE_QUALIFICATION_RESULTS_REQUEST_BODY)
                .when()
                .put(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong number of attempts.'

        and:
        championshipStageParticipantRepository.findById(participant.id).get() == participant
    }


    // --- Qualification results retrieval

    def 'should provide qualification results successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageParticipantRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(UPDATED_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_3.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        ])

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(Map)

        then:
        def qualificationResults = response as Map
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_1.userId) == 244d
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_2.userId) == 70.95d
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_3.userId) == 0d
    }

    def 'should not provide qualification results if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not provide qualification results if request without valid access token'() {
        expect:
        when()
                .get(QUALIFICATION_RESULTS_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Qualification results retrieval by judge

    def 'should provide qualification results by judge successfully'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)
        championshipStageJudgeRepository.saveAndFlush(CHAMPIONSHIP_STAGE_JUDGE_3)
        championshipStageParticipantRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE_PARTICIPANT_1.toBuilder().qualificationResults(QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_2.toBuilder().qualificationResults(UPDATED_QUALIFICATION_RESULTS).build(),
                CHAMPIONSHIP_STAGE_PARTICIPANT_3.toBuilder().qualificationResults(INITIAL_QUALIFICATION_RESULTS).build()
        ])

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(QUALIFICATION_RESULTS_BY_JUDGE_API_URL)
                .then()
                .statusCode(SC_OK)
                .extract().body().as(Map)

        then:
        def qualificationResults = response as Map
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_1.userId) as List == [72d, 67.5d, 81.5d]
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_2.userId) as List == [67d, 70.95d, 0d]
        qualificationResults.get(CHAMPIONSHIP_STAGE_PARTICIPANT_3.userId) as List == [0d, 0d, 0d]
    }

    def 'should not provide qualification results by judge if wrong championship stage id is provided'() {
        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(QUALIFICATION_RESULTS_BY_JUDGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong championship stage id.'
    }

    def 'should not provide qualification results by judge if wrong judge user id is provided'() {
        given:
        championshipStageRepository.saveAndFlush(CHAMPIONSHIP_STAGE)

        when:
        def response = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .when()
                .get(QUALIFICATION_RESULTS_BY_JUDGE_API_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .extract().body().asString()

        then:
        response == 'Validation exception: Wrong judge user id.'
    }

    def 'should not provide qualification results by judge if request without valid access token'() {
        expect:
        when()
                .get(QUALIFICATION_RESULTS_BY_JUDGE_API_URL)
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
