package drift.controller;

import drift.dto.ChampionshipStageCreationDto;
import drift.dto.ChampionshipStageJudgesAssignmentDto;
import drift.dto.ChampionshipStageUpdateDto;
import drift.dto.QualificationResultsDto;
import drift.service.ChampionshipStageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/championship-stages")
@RequiredArgsConstructor
public class ChampionshipStageController {

    private final ChampionshipStageService championshipStageService;

    @Operation(summary = "Create championship stage")
    @PostMapping
    public String createChampionshipStage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                          @RequestBody @Valid ChampionshipStageCreationDto dto) {
        return championshipStageService.createChampionshipStage(dto);
    }

    @Operation(summary = "Update championship stage")
    @PutMapping("/{championshipStageId}")
    public void updateChampionshipStage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                        @PathVariable String championshipStageId,
                                        @RequestBody @Valid ChampionshipStageUpdateDto dto) {
        championshipStageService.updateChampionshipStage(championshipStageId, dto);
    }

    @Operation(summary = "Deactivate championship stage")
    @DeleteMapping("/{championshipStageId}")
    public void deactivateChampionshipStage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                            @PathVariable String championshipStageId) {
        championshipStageService.deactivateChampionshipStage(championshipStageId);
    }

    @Operation(summary = "Upload championship stage placard image")
    @PostMapping(value = "/{championshipStageId}/placard-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadPlacardImage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                   @PathVariable String championshipStageId,
                                   @RequestParam MultipartFile image) {
        championshipStageService.uploadPlacardImage(championshipStageId, image);
    }

    @Operation(summary = "Register currently signed in user as championship stage participant")
    @PostMapping("/{championshipStageId}/participants")
    public void registerParticipant(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                    @PathVariable String championshipStageId) {
        championshipStageService.registerParticipant(championshipStageId);
    }

    @Operation(summary = "Unregister currently signed in user from championship stage")
    @DeleteMapping("/{championshipStageId}/participants")
    public void deleteParticipant(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                  @PathVariable String championshipStageId) {
        championshipStageService.deleteParticipant(championshipStageId);
    }

    @Operation(summary = "Get all participants")
    @GetMapping("/{championshipStageId}/participants")
    public Collection<String> getParticipants(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                              @PathVariable String championshipStageId) {
        return championshipStageService.getParticipants(championshipStageId);
    }

    @Operation(summary = "Assign judges")
    @PostMapping("/{championshipStageId}/judges")
    public void assignJudges(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                             @PathVariable String championshipStageId,
                             @RequestBody @Valid ChampionshipStageJudgesAssignmentDto dto) {
        championshipStageService.assignJudges(championshipStageId, dto.getUserIds());
    }

    @Operation(summary = "Get judges")
    @GetMapping("/{championshipStageId}/judges")
    public Collection<String> getJudges(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                        @PathVariable String championshipStageId) {
        return championshipStageService.getJudges(championshipStageId);
    }

    @Operation(summary = "Start qualification phase")
    @PostMapping("/{championshipStageId}/start-qualification")
    public void startQualification(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                   @PathVariable String championshipStageId) {
        championshipStageService.startQualification(championshipStageId);
    }

    @Operation(summary = "Update qualification results")
    @PutMapping("/{championshipStageId}/qualification-results")
    public void updateQualificationResults(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                           @PathVariable String championshipStageId,
                                           @RequestBody @Valid QualificationResultsDto dto) {
        championshipStageService.updateQualificationResults(championshipStageId, dto);
    }

    @Operation(summary = "Get qualification results. Returns participant user id to best attempt result map.")
    @GetMapping("/{championshipStageId}/qualification-results")
    public Map<String, Double> getQualificationResults(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                       @PathVariable String championshipStageId) {
        return championshipStageService.getQualificationResults(championshipStageId);
    }

    @Operation(summary = "Get qualification results. Returns participant user id to attempts points map provided by judge.")
    @GetMapping("/{championshipStageId}/qualification-results/by-judge/{judgeUserId}")
    public Map<String, List<Double>> getQualificationResults(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                                             @PathVariable String championshipStageId,
                                                             @PathVariable String judgeUserId) {
        return championshipStageService.getQualificationResults(championshipStageId, judgeUserId);
    }
}
