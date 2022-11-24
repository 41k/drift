package drift.controller;

import drift.dto.*;
import drift.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final OrganisationRepository organisationRepository;
    private final ChampionshipRepository championshipRepository;
    private final ChampionshipStageRepository championshipStageRepository;
    private final TrainingRepository trainingRepository;

    @Operation(summary = "Search for users")
    @PostMapping("/users")
    public Collection<UserDto> searchForUsers(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                              @RequestBody UsersSearchContext context) {
        return userRepository.search(context).stream().map(UserDto::from).collect(Collectors.toList());
    }

    @Operation(summary = "Search for cars")
    @PostMapping("/cars")
    public Collection<CarDto> searchForCars(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                            @RequestBody CarsSearchContext context) {
        return carRepository.search(context).stream().map(CarDto::from).collect(Collectors.toList());
    }

    @Operation(summary = "Search for organisations")
    @PostMapping("/organisations")
    public Collection<OrganisationDto> searchForOrganisations(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                              @RequestBody OrganisationsSearchContext context) {
        return organisationRepository.search(context).stream().map(OrganisationDto::from).collect(Collectors.toList());
    }

    @Operation(summary = "Search for championships")
    @PostMapping("/championships")
    public Collection<ChampionshipDto> searchForChampionships(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                              @RequestBody ChampionshipsSearchContext context) {
        return championshipRepository.search(context).stream().map(ChampionshipDto::from).collect(Collectors.toList());
    }

    @Operation(summary = "Search for championship stages")
    @PostMapping("/championship-stages")
    public Collection<ChampionshipStageDto> searchForChampionshipStages(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                                        @RequestBody ChampionshipStagesSearchContext context) {
        return championshipStageRepository.search(context).stream().map(ChampionshipStageDto::from).collect(Collectors.toList());
    }

    @Operation(summary = "Search for trainings")
    @PostMapping("/trainings")
    public Collection<TrainingDto> searchForTrainings(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                      @RequestBody TrainingsSearchContext context) {
        return trainingRepository.search(context).stream().map(TrainingDto::from).collect(Collectors.toList());
    }
}
