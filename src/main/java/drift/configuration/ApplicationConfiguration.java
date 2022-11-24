package drift.configuration;

import drift.configuration.properties.StaticContentStorageProperties;
import drift.repository.*;
import drift.service.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(StaticContentStorageProperties.class)
public class ApplicationConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public FileService fileService(StaticContentStorageProperties properties) {
        return new FileService(properties);
    }

    @Bean
    public UserService userService(
            IdGenerator idGenerator,
            UserRepository userRepository,
            SecurityService securityService,
            MailService mailService,
            FileService fileService
    ) {
        return new UserService(idGenerator, userRepository, securityService, mailService, fileService);
    }

    @Bean
    public RoleService roleService(
            CarRepository carRepository,
            OrganisationRepository organisationRepository
    ) {
        return new RoleService(carRepository, organisationRepository);
    }

    @Bean
    public CarService carService(
            IdGenerator idGenerator,
            SecurityService securityService,
            CarRepository carRepository,
            FileService fileService
    ) {
        return new CarService(idGenerator, securityService, carRepository, fileService);
    }

    @Bean
    public OrganisationService organisationService(
            IdGenerator idGenerator,
            SecurityService securityService,
            OrganisationRepository organisationRepository,
            FileService fileService
    ) {
        return new OrganisationService(idGenerator, securityService, organisationRepository, fileService);
    }

    @Bean
    public ScoringSystemService scoringSystemService(
            IdGenerator idGenerator,
            SecurityService securityService,
            ScoringSystemRepository scoringSystemRepository
    ) {
        return new ScoringSystemService(idGenerator, securityService, scoringSystemRepository);
    }

    @Bean
    public ChampionshipService championshipService(
            IdGenerator idGenerator,
            SecurityService securityService,
            OrganisationService organisationService,
            ChampionshipRepository championshipRepository
    ) {
        return new ChampionshipService(idGenerator, securityService, organisationService, championshipRepository);
    }

    @Bean
    public ChampionshipStageService championshipStageService(
            IdGenerator idGenerator,
            SecurityService securityService,
            ChampionshipService championshipService,
            UserService userService,
            ChampionshipStageRepository championshipStageRepository,
            ChampionshipStageParticipantRepository championshipStageParticipantRepository,
            ChampionshipStageJudgeRepository championshipStageJudgeRepository,
            FileService fileService
    ) {
        return new ChampionshipStageService(
                idGenerator, securityService, championshipService, userService, championshipStageRepository,
                championshipStageParticipantRepository, championshipStageJudgeRepository, fileService);
    }

    @Bean
    public TrainingService trainingService(
            IdGenerator idGenerator,
            SecurityService securityService,
            OrganisationService organisationService,
            TrainingRepository trainingRepository,
            TrainingParticipantRepository trainingParticipantRepository,
            FileService fileService
    ) {
        return new TrainingService(
                idGenerator, securityService, organisationService,
                trainingRepository, trainingParticipantRepository, fileService);
    }
}
