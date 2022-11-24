package drift.service;

import drift.dto.OrganisationRegistrationDto;
import drift.dto.OrganisationUpdateDto;
import drift.model.ImageCategory;
import drift.model.Organisation;
import drift.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;

@Transactional
@RequiredArgsConstructor
public class OrganisationService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final OrganisationRepository organisationRepository;
    private final FileService fileService;

    public String registerOrganisation(OrganisationRegistrationDto dto) {
        return organisationRepository.save(
                Organisation.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .active(true)
                        .build()
        ).getId();
    }

    public void updateOrganisation(String organisationId, OrganisationUpdateDto dto) {
        var organisation = getRequesterOrganisation(organisationId);
        organisationRepository.save(organisation.toBuilder().description(dto.getDescription()).build());
    }

    public void deactivateOrganisation(String organisationId) {
        var organisation = getRequesterOrganisation(organisationId);
        organisationRepository.save(organisation.toBuilder().active(false).build());
    }

    public void uploadImage(String organisationId, MultipartFile image) {
        var organisation = getRequesterOrganisation(organisationId);
        var imagePath = fileService.uploadImage(ImageCategory.ORGANISATION, organisationId, image);
        var updatedOrganisation = organisation.toBuilder().image(imagePath).build();
        organisationRepository.save(updatedOrganisation);
    }

    public Organisation getRequesterOrganisation(String organisationId) {
        var organisation = organisationRepository.findById(organisationId).orElseThrow(wrongOrganisationIdException());
        securityService.validateOwner(organisation.getOwnerId(), Organisation.class);
        return organisation;
    }

    private Supplier<IllegalArgumentException> wrongOrganisationIdException() {
        return () -> new IllegalArgumentException("Wrong organisation id");
    }
}
