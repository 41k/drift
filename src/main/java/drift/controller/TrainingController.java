package drift.controller;

import drift.dto.TrainingCreationDto;
import drift.dto.TrainingUpdateDto;
import drift.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collection;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @Operation(summary = "Create training")
    @PostMapping
    public String createTraining(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                 @RequestBody @Valid TrainingCreationDto dto) {
        return trainingService.createTraining(dto);
    }

    @Operation(summary = "Update training")
    @PutMapping("/{trainingId}")
    public void updateTraining(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                               @PathVariable String trainingId,
                               @RequestBody @Valid TrainingUpdateDto dto) {
        trainingService.updateTraining(trainingId, dto);
    }

    @Operation(summary = "Deactivate training")
    @DeleteMapping("/{trainingId}")
    public void deactivateTraining(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                   @PathVariable String trainingId) {
        trainingService.deactivateTraining(trainingId);
    }

    @Operation(summary = "Upload training placard image")
    @PostMapping(value = "/{trainingId}/placard-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadPlacardImage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                   @PathVariable String trainingId,
                                   @RequestParam MultipartFile image) {
        trainingService.uploadPlacardImage(trainingId, image);
    }

    @Operation(summary = "Register currently signed in user as training participant")
    @PostMapping("/{trainingId}/participants")
    public void registerParticipant(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                    @PathVariable String trainingId) {
        trainingService.registerParticipant(trainingId);
    }

    @Operation(summary = "Unregister currently signed in user from training")
    @DeleteMapping("/{trainingId}/participants")
    public void deleteParticipant(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                  @PathVariable String trainingId) {
        trainingService.deleteParticipant(trainingId);
    }

    @Operation(summary = "Get all training participants")
    @GetMapping("/{trainingId}/participants")
    public Collection<String> getParticipants(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                              @PathVariable String trainingId) {
        return trainingService.getParticipants(trainingId);
    }
}
