package drift.controller;

import drift.dto.OrganisationRegistrationDto;
import drift.dto.OrganisationUpdateDto;
import drift.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/organisations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @Operation(summary = "Create organisation")
    @PostMapping
    public String registerOrganisation(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                       @RequestBody @Valid OrganisationRegistrationDto dto) {
        return organisationService.registerOrganisation(dto);
    }

    @Operation(summary = "Update organisation")
    @PutMapping("/{organisationId}")
    public void updateOrganisation(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                   @PathVariable String organisationId,
                                   @RequestBody @Valid OrganisationUpdateDto dto) {
        organisationService.updateOrganisation(organisationId, dto);
    }

    @Operation(summary = "Deactivate organisation")
    @DeleteMapping("/{organisationId}")
    public void deactivateOrganisation(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                       @PathVariable String organisationId) {
        organisationService.deactivateOrganisation(organisationId);
    }

    @Operation(summary = "Upload organisation image")
    @PostMapping(value = "/{organisationId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadImage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                            @PathVariable String organisationId,
                            @RequestParam MultipartFile image) {
        organisationService.uploadImage(organisationId, image);
    }
}
