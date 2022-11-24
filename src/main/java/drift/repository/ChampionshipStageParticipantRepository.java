package drift.repository;

import drift.model.ChampionshipStageParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ChampionshipStageParticipantRepository extends JpaRepository<ChampionshipStageParticipant, String> {

    Optional<ChampionshipStageParticipant> findByChampionshipStageIdAndUserId(String championshipStageId, String userId);

    boolean existsByChampionshipStageIdAndUserId(String championshipStageId, String userId);

    void deleteByChampionshipStageIdAndUserId(String championshipStageId, String userId);

    long countByChampionshipStageId(String championshipStageId);

    Collection<ChampionshipStageParticipant> findAllByChampionshipStageId(String championshipStageId);
}
