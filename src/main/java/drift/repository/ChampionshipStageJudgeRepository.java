package drift.repository;

import drift.model.ChampionshipStageJudge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ChampionshipStageJudgeRepository extends JpaRepository<ChampionshipStageJudge, String> {

    boolean existsByChampionshipStageIdAndUserId(String championshipStageId, String userId);

    void deleteAllByChampionshipStageId(String championshipStageId);

    long countByChampionshipStageId(String championshipStageId);

    Collection<ChampionshipStageJudge> findAllByChampionshipStageId(String championshipStageId);
}
