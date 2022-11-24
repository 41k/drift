package drift.controller;

import drift.dto.ChampionshipCreationDto;
import drift.service.ChampionshipService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/championships")
@RequiredArgsConstructor
public class ChampionshipController {

    private final ChampionshipService championshipService;

    @Operation(summary = "Create championship")
    @PostMapping
    public String createChampionship(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                     @RequestBody @Valid ChampionshipCreationDto dto) {
        return championshipService.createChampionship(dto);
    }

    @Operation(summary = "Deactivate championship")
    @DeleteMapping("/{championshipId}")
    public void deactivateChampionship(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                       @PathVariable String championshipId) {
        championshipService.deactivateChampionship(championshipId);
    }
}
