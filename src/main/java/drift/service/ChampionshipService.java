package drift.service;

import drift.dto.ChampionshipCreationDto;
import drift.model.Championship;
import drift.repository.ChampionshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Transactional
@RequiredArgsConstructor
public class ChampionshipService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final OrganisationService organisationService;
    private final ChampionshipRepository championshipRepository;

    public String createChampionship(ChampionshipCreationDto dto) {
        organisationService.getRequesterOrganisation(dto.getOrganisationId());
        return championshipRepository.save(
                Championship.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .organisationId(dto.getOrganisationId())
                        .discipline(dto.getDiscipline())
                        .scoringSystemId(dto.getScoringSystemId())
                        .active(true)
                        .build()
        ).getId();
    }

    public void deactivateChampionship(String championshipId) {
        var championship = getRequesterChampionship(championshipId);
        championshipRepository.save(championship.toBuilder().active(false).build());
    }

    public Championship getRequesterChampionship(String championshipId) {
        var championship = championshipRepository.findById(championshipId).orElseThrow(wrongChampionshipIdException());
        securityService.validateOwner(championship.getOwnerId(), Championship.class);
        return championship;
    }

    private Supplier<IllegalArgumentException> wrongChampionshipIdException() {
        return () -> new IllegalArgumentException("Wrong championship id");
    }
}
