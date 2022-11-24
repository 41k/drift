package drift.controller;

import drift.dto.ScoringSystemCreationDto;
import drift.dto.ScoringSystemDto;
import drift.service.ScoringSystemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/scoring-systems")
@RequiredArgsConstructor
public class ScoringSystemController {

    private final ScoringSystemService scoringSystemService;

    @Operation(summary = "Create scoring system")
    @PostMapping
    public String createScoringSystem(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                      @RequestBody @Valid ScoringSystemCreationDto dto) {
        return scoringSystemService.createScoringSystem(dto);
    }

    @Operation(summary = "Get scoring systems")
    @GetMapping
    public Collection<ScoringSystemDto> getScoringSystems(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken) {
        return scoringSystemService.getScoringSystems();
    }

    @Operation(summary = "Get scoring system by id")
    @GetMapping("/{scoringSystemId}")
    public ScoringSystemDto getScoringSystem(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                             @PathVariable String scoringSystemId) {
        return scoringSystemService.getScoringSystem(scoringSystemId);
    }
}
