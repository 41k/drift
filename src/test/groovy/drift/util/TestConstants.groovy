package drift.util

import com.fasterxml.jackson.databind.ObjectMapper
import drift.dto.*
import drift.model.*

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TestConstants {

    public static final OBJECT_MAPPER = new ObjectMapper()

    public static final EXCEPTION_MESSAGE = 'exception-message'

    public static final USER_ID_1 = 'user-id-1'
    public static final USER_ID_2 = 'user-id-2'
    public static final USER_ID_3 = 'user-id-3'
    public static final EMAIL = 'eMaiL-1@mail.com'
    public static final NORMALIZED_EMAIL = 'email-1@mail.com'
    public static final PASSWORD_1 = 'password-1'
    public static final PASSWORD_1_ENCODED = 'password-1-encoded'
    public static final PASSWORD_2 = 'password-2'
    public static final PASSWORD_2_ENCODED = 'password-2-encoded'
    public static final VERIFICATION_CODE = 'verification-code'
    public static final COUNTRY_CODE = 'US'
    public static final CITY = 'NEW-YORK'
    public static final ACCESS_TOKEN = 'access-token'
    public static final FIRST_NAME = 'first-name'
    public static final LAST_NAME = 'last-name'
    public static final PATH = 'path-to-file'

    public static final USER_REGISTRATION_DTO = new UserRegistrationDto(email: EMAIL, password: PASSWORD_1)
    public static final USER_ACTIVATION_DTO = new UserActivationDto(email: EMAIL, verificationCode: VERIFICATION_CODE)
    public static final SIGN_IN_DTO = new SignInDto(email: EMAIL, password: PASSWORD_1)
    public static final RESET_PASSWORD_DTO = new ResetPasswordDto(email: EMAIL)
    public static final USER_UPDATE_DTO = new UserUpdateDto(
            password: PASSWORD_2,
            firstName: FIRST_NAME,
            lastName: LAST_NAME,
            countryCode: COUNTRY_CODE,
            city: CITY
    )

    public static final TIMESTAMP = 1641273825000L
    public static final CLOCK = Clock.fixed(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault())

    public static final JSON_CONTENT_TYPE = 'application/json'
    public static final BASE_API_V1_URL = '/api/v1'
    public static final BASE_AUTH_API_URL = "$BASE_API_V1_URL/auth"
    public static final ME_API_URL = "$BASE_API_V1_URL/me"
    public static final UPLOAD_MY_IMAGE_API_URL = "$ME_API_URL/image"
    public static final USER_URI = "$BASE_API_V1_URL/users/$USER_ID_1"
    public static final ROLES_URI = "$USER_URI/roles"

    public static final CAR_ID = 'car-id'
    public static final BRAND = 'brand'
    public static final MODEL = 'model'
    public static final POWER_1 = 310.5d
    public static final POWER_2 = 220.3d

    public static final CAR = Car.builder()
            .id(CAR_ID).ownerId(USER_ID_1).brand(BRAND)
            .model(MODEL).power(POWER_1).active(true).build()
    public static final CAR_REGISTRATION_DTO = new CarRegistrationDto(brand: BRAND, model: MODEL, power: POWER_1)
    public static final CAR_UPDATE_DTO = new CarUpdateDto(power: POWER_2)

    public static final BASE_CARS_API_URL = "$BASE_API_V1_URL/cars"
    public static final CAR_URI = "$BASE_CARS_API_URL/$CAR_ID"
    public static final UPLOAD_CAR_IMAGE_API_URL = "$CAR_URI/image"

    public static final ORGANISATION_ID = 'organisation-id'
    public static final NAME_1 = 'name-1'
    public static final NAME_2 = 'name-2'
    public static final DESCRIPTION_1 = 'description-1'
    public static final DESCRIPTION_2 = 'description-2'

    public static final ORGANISATION = Organisation.builder()
            .id(ORGANISATION_ID).ownerId(USER_ID_1).name(NAME_1).description(DESCRIPTION_1).active(true).build()
    public static final ORGANISATION_REGISTRATION_DTO = new OrganisationRegistrationDto(name: NAME_1, description: DESCRIPTION_1)
    public static final ORGANISATION_UPDATE_DTO = new OrganisationUpdateDto(description: DESCRIPTION_2)

    public static final BASE_ORGANISATIONS_API_URL = "$BASE_API_V1_URL/organisations"
    public static final ORGANISATION_URI = "$BASE_ORGANISATIONS_API_URL/$ORGANISATION_ID"
    public static final UPLOAD_ORGANISATION_IMAGE_API_URL = "$ORGANISATION_URI/image"

    public static final SCORING_SYSTEM_ID = 'scoring-system-id'
    public static final PARTICIPATION_POINTS = 1.5d
    public static final QUALIFICATION_POINTS = [5.1d, 4.1d, 3.1d, 2.1d, 1.1d]
    public static final QUALIFICATION_POINTS_AS_JSON = OBJECT_MAPPER.writeValueAsString(QUALIFICATION_POINTS)
    public static final POINTS = [50.1d, 40.1d, 30.1d, 20.1d, 10.1d]
    public static final POINTS_AS_JSON = OBJECT_MAPPER.writeValueAsString(POINTS)
    public static final PARTICIPANTS_AFTER_QUALIFICATION = 8
    public static final SCORING_SYSTEM = ScoringSystem.builder()
            .id(SCORING_SYSTEM_ID).ownerId(USER_ID_1).name(NAME_1).participationPoints(PARTICIPATION_POINTS)
            .qualificationPoints(QUALIFICATION_POINTS).points(POINTS)
            .participantsAfterQualification(PARTICIPANTS_AFTER_QUALIFICATION).active(true).build()
    public static final SCORING_SYSTEM_CREATION_DTO = new ScoringSystemCreationDto(
            name: NAME_1,
            participationPoints: PARTICIPATION_POINTS,
            qualificationPoints: QUALIFICATION_POINTS,
            points: POINTS,
            participantsAfterQualification: PARTICIPANTS_AFTER_QUALIFICATION
    )
    public static final SCORING_SYSTEM_DTO = ScoringSystemDto.builder()
            .id(SCORING_SYSTEM_ID).name(NAME_1).participationPoints(PARTICIPATION_POINTS)
            .qualificationPoints(QUALIFICATION_POINTS).points(POINTS)
            .participantsAfterQualification(PARTICIPANTS_AFTER_QUALIFICATION).build()

    public static final BASE_SCORING_SYSTEMS_API_URL = "$BASE_API_V1_URL/scoring-systems"
    public static final SCORING_SYSTEM_URI = "$BASE_SCORING_SYSTEMS_API_URL/$SCORING_SYSTEM_ID"

    public static final CHAMPIONSHIP_ID = 'championship-id'
    public static final DISCIPLINE = Discipline.DRIFT

    public static final CHAMPIONSHIP = Championship.builder()
            .id(CHAMPIONSHIP_ID).ownerId(USER_ID_1).organisationId(ORGANISATION_ID)
            .discipline(DISCIPLINE).scoringSystemId(SCORING_SYSTEM_ID).active(true).build()
    public static final CHAMPIONSHIP_CREATION_DTO = new ChampionshipCreationDto(
            organisationId: ORGANISATION_ID,
            discipline: DISCIPLINE,
            scoringSystemId: SCORING_SYSTEM_ID
    )
    public static final CHAMPIONSHIP_DTO = ChampionshipDto.builder()
            .id(CHAMPIONSHIP_ID).ownerId(USER_ID_1).organisationId(ORGANISATION_ID)
            .discipline(DISCIPLINE).scoringSystemId(SCORING_SYSTEM_ID).active(true).build()

    public static final BASE_CHAMPIONSHIPS_API_URL = "$BASE_API_V1_URL/championships"
    public static final CHAMPIONSHIP_URI = "$BASE_CHAMPIONSHIPS_API_URL/$CHAMPIONSHIP_ID"

    public static final CHAMPIONSHIP_STAGE_ID = 'championship-stage-id'
    public static final TIMESTAMP_1 = 1656066464000L
    public static final TIMESTAMP_2 = 1656067464000L
    public static final TIMESTAMP_1_AS_INSTANT = Instant.ofEpochMilli(TIMESTAMP_1)
    public static final TIMESTAMP_2_AS_INSTANT = Instant.ofEpochMilli(TIMESTAMP_2)
    public static final DURATION_1 = 5
    public static final DURATION_2 = 7
    public static final LOCATION_1 = 'location-1'
    public static final LOCATION_2 = 'location-2'
    public static final PARTICIPATION_INFO_1 = 'participation-info-1'
    public static final PARTICIPATION_INFO_2 = 'participation-info-2'
    public static final N_ATTEMPTS_1 = 3
    public static final N_ATTEMPTS_2 = 2
    public static final N_OMT_1 = 4
    public static final N_OMT_2 = 8

    public static final CHAMPIONSHIP_STAGE = ChampionshipStage.builder()
            .id(CHAMPIONSHIP_STAGE_ID).ownerId(USER_ID_1).championshipId(CHAMPIONSHIP_ID)
            .name(NAME_1).duration(DURATION_1).startTimestamp(TIMESTAMP_1_AS_INSTANT)
            .location(LOCATION_1).description(DESCRIPTION_1).participationInfo(PARTICIPATION_INFO_1)
            .attempts(N_ATTEMPTS_1).omt(N_OMT_1).phase(ChampionshipStagePhase.CREATION).active(true).build()
    public static final CHAMPIONSHIP_STAGE_CREATION_DTO = new ChampionshipStageCreationDto(
            championshipId: CHAMPIONSHIP_ID,
            name: NAME_1,
            duration: DURATION_1,
            startTimestamp: TIMESTAMP_1,
            location: LOCATION_1,
            description: DESCRIPTION_1,
            participationInfo: PARTICIPATION_INFO_1,
            attempts: N_ATTEMPTS_1,
            omt: N_OMT_1
    )
    public static final CHAMPIONSHIP_STAGE_UPDATE_DTO = new ChampionshipStageUpdateDto(
            name: NAME_2,
            duration: DURATION_2,
            startTimestamp: TIMESTAMP_2,
            location: LOCATION_2,
            description: DESCRIPTION_2,
            participationInfo: PARTICIPATION_INFO_2,
            attempts: N_ATTEMPTS_2,
            omt: N_OMT_2
    )

    public static final BASE_CHAMPIONSHIP_STAGES_API_URL = "$BASE_API_V1_URL/championship-stages"
    public static final CHAMPIONSHIP_STAGE_URI = "$BASE_CHAMPIONSHIP_STAGES_API_URL/$CHAMPIONSHIP_STAGE_ID"
    public static final UPLOAD_CHAMPIONSHIP_STAGE_PLACARD_IMAGE_API_URL = "$CHAMPIONSHIP_STAGE_URI/placard-image"
    public static final CHAMPIONSHIP_STAGE_PARTICIPANTS_API_URL = "$CHAMPIONSHIP_STAGE_URI/participants"
    public static final CHAMPIONSHIP_STAGE_JUDGES_API_URL = "$CHAMPIONSHIP_STAGE_URI/judges"
    public static final START_QUALIFICATION_API_URL = "$CHAMPIONSHIP_STAGE_URI/start-qualification"
    public static final QUALIFICATION_RESULTS_API_URL = "$CHAMPIONSHIP_STAGE_URI/qualification-results"
    public static final QUALIFICATION_RESULTS_BY_JUDGE_API_URL = "$CHAMPIONSHIP_STAGE_URI/qualification-results/by-judge/$USER_ID_3"

    public static final CHAMPIONSHIP_STAGE_JUDGE_ID_1 = 'championship-stage-judge-id-1'
    public static final CHAMPIONSHIP_STAGE_JUDGE_ID_2 = 'championship-stage-judge-id-2'
    public static final CHAMPIONSHIP_STAGE_JUDGE_ID_3 = 'championship-stage-judge-id-3'
    public static final CHAMPIONSHIP_STAGE_JUDGE_1 = ChampionshipStageJudge.builder()
            .id(CHAMPIONSHIP_STAGE_JUDGE_ID_1).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_1).build()
    public static final CHAMPIONSHIP_STAGE_JUDGE_2 = ChampionshipStageJudge.builder()
            .id(CHAMPIONSHIP_STAGE_JUDGE_ID_2).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_2).build()
    public static final CHAMPIONSHIP_STAGE_JUDGE_3 = ChampionshipStageJudge.builder()
            .id(CHAMPIONSHIP_STAGE_JUDGE_ID_3).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_3).build()

    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1 = 'championship-stage-participant-id-1'
    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_ID_2 = 'championship-stage-participant-id-2'
    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_ID_3 = 'championship-stage-participant-id-3'
    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_1 = ChampionshipStageParticipant.builder()
            .id(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_1).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_1).build()
    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_2 = ChampionshipStageParticipant.builder()
            .id(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_2).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_2).build()
    public static final CHAMPIONSHIP_STAGE_PARTICIPANT_3 = ChampionshipStageParticipant.builder()
            .id(CHAMPIONSHIP_STAGE_PARTICIPANT_ID_3).championshipStageId(CHAMPIONSHIP_STAGE_ID).userId(USER_ID_3).build()

    public static final INITIAL_QUALIFICATION_RESULTS = [
            (CHAMPIONSHIP_STAGE_JUDGE_1.userId) : [0d, 0d, 0d],
            (CHAMPIONSHIP_STAGE_JUDGE_2.userId) : [0d, 0d, 0d],
            (CHAMPIONSHIP_STAGE_JUDGE_3.userId) : [0d, 0d, 0d]
    ]
    public static final UPDATED_ATTEMPTS_POINTS = [67d, 70.95d, 0d]
    public static final UPDATED_QUALIFICATION_RESULTS = [
            (CHAMPIONSHIP_STAGE_JUDGE_1.userId) : [0d, 0d, 0d],
            (CHAMPIONSHIP_STAGE_JUDGE_2.userId) : [0d, 0d, 0d],
            (CHAMPIONSHIP_STAGE_JUDGE_3.userId) : UPDATED_ATTEMPTS_POINTS
    ]
    public static final QUALIFICATION_RESULTS = [
            (CHAMPIONSHIP_STAGE_JUDGE_1.userId) : [70.5d, 67d, 82.5d],
            (CHAMPIONSHIP_STAGE_JUDGE_2.userId) : [69d, 68.5d, 80d],
            (CHAMPIONSHIP_STAGE_JUDGE_3.userId) : [72d, 67.5d, 81.5d]
    ]

    public static final TRAINING_ID = 'training-id'
    public static final TRAINING = Training.builder()
            .id(TRAINING_ID).ownerId(USER_ID_1).organisationId(ORGANISATION_ID)
            .discipline(DISCIPLINE).name(NAME_1).startTimestamp(TIMESTAMP_1_AS_INSTANT)
            .location(LOCATION_1).participationInfo(PARTICIPATION_INFO_1).active(true).build()
    public static final TRAINING_CREATION_DTO = new TrainingCreationDto(
            organisationId: ORGANISATION_ID,
            discipline: DISCIPLINE,
            name: NAME_1,
            startTimestamp: TIMESTAMP_1,
            location: LOCATION_1,
            participationInfo: PARTICIPATION_INFO_1
    )
    public static final TRAINING_UPDATE_DTO = new TrainingUpdateDto(
            name: NAME_2,
            startTimestamp: TIMESTAMP_2,
            location: LOCATION_2,
            participationInfo: PARTICIPATION_INFO_2
    )
    public static final BASE_TRAININGS_API_URL = "$BASE_API_V1_URL/trainings"
    public static final TRAINING_URI = "$BASE_TRAININGS_API_URL/$TRAINING_ID"
    public static final UPLOAD_TRAINING_PLACARD_IMAGE_API_URL = "$TRAINING_URI/placard-image"
    public static final TRAINING_PARTICIPANTS_API_URL = "$TRAINING_URI/participants"

    public static final TRAINING_PARTICIPANT_ID = 'training-participant-id'
    public static final TRAINING_PARTICIPANT = TrainingParticipant.builder()
            .id(TRAINING_PARTICIPANT_ID).trainingId(TRAINING_ID).userId(USER_ID_1).build()

    public static final BASE_SEARCH_API_URL = "$BASE_API_V1_URL/search"

    public static final USER_IMAGE_PATH = '/images/users/' + USER_ID_1 + '.png'
    public static final CAR_IMAGE_PATH = '/images/cars/' + CAR_ID + '.png'
    public static final ORGANISATION_IMAGE_PATH = '/images/organisations/' + ORGANISATION_ID + '.png'
    public static final CHAMPIONSHIP_STAGE_IMAGE_PATH = '/images/championship-stages/' + CHAMPIONSHIP_STAGE_ID + '.png'
    public static final TRAINING_IMAGE_PATH = '/images/trainings/' + TRAINING_ID + '.png'

    public static final IMAGE = new File(ClassLoader.getSystemResource('files-to-upload/image.png').toURI())
    public static final TEXT_FILE = new File(ClassLoader.getSystemResource('files-to-upload/text-file.txt').toURI())
}
