package drift.functional

import com.fasterxml.jackson.databind.ObjectMapper
import drift.repository.*
import drift.service.SecurityService
import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Clock

import static drift.util.TestConstants.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(['test'])
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [BaseTestContextConfiguration])
abstract class BaseFunctionalTest extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    protected UserRepository userRepository
    @Autowired
    protected CarRepository carRepository
    @Autowired
    protected OrganisationRepository organisationRepository
    @Autowired
    protected ScoringSystemRepository scoringSystemRepository
    @Autowired
    protected ChampionshipRepository championshipRepository
    @Autowired
    protected ChampionshipStageRepository championshipStageRepository
    @Autowired
    protected ChampionshipStageParticipantRepository championshipStageParticipantRepository
    @Autowired
    protected ChampionshipStageJudgeRepository championshipStageJudgeRepository
    @Autowired
    protected TrainingRepository trainingRepository
    @Autowired
    protected TrainingParticipantRepository trainingParticipantRepository

    @Autowired
    protected SecurityService securityService

    protected String accessTokenForUser1
    protected String accessTokenForUser2

    def setup() {
        RestAssured.port = port
        accessTokenForUser1 = securityService.generateAccessToken(USER_ID_1)
        accessTokenForUser2 = securityService.generateAccessToken(USER_ID_2)
        deleteFiles(
                USER_IMAGE_PATH,
                CAR_IMAGE_PATH,
                ORGANISATION_IMAGE_PATH,
                CHAMPIONSHIP_STAGE_IMAGE_PATH,
                TRAINING_IMAGE_PATH
        )
        userRepository.deleteAll()
        userRepository.flush()
        carRepository.deleteAll()
        carRepository.flush()
        organisationRepository.deleteAll()
        organisationRepository.flush()
        championshipRepository.deleteAll()
        championshipRepository.flush()
        championshipStageRepository.deleteAll()
        championshipStageRepository.flush()
        championshipStageParticipantRepository.deleteAll()
        championshipStageParticipantRepository.flush()
        championshipStageJudgeRepository.deleteAll()
        championshipStageJudgeRepository.flush()
        trainingRepository.deleteAll()
        trainingRepository.flush()
        trainingParticipantRepository.deleteAll()
        trainingParticipantRepository.flush()
    }

    private static void deleteFiles(String... filePaths) {
        filePaths.each { Files.deleteIfExists(Paths.get(it)) }
    }

    @TestConfiguration
    static class BaseTestContextConfiguration {
        @Bean
        Clock clock() { CLOCK }
    }
}
