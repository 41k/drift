package drift.repository;

import drift.model.ChampionshipStage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.Collection;

public interface ChampionshipStageRepository extends JpaRepository<ChampionshipStage, String>, JpaSpecificationExecutor<ChampionshipStage> {

    default Collection<ChampionshipStage> search(ChampionshipStagesSearchContext context) {
        var specifications = new ArrayList<Specification<ChampionshipStage>>();
        if (CollectionUtils.isNotEmpty(context.getChampionshipStageIds())) {
            specifications.add((root, query, builder) -> root.get("id").in(context.getChampionshipStageIds()));
        }
        if (CollectionUtils.isNotEmpty(context.getChampionshipIds())) {
            specifications.add((root, query, builder) -> root.get("championshipId").in(context.getChampionshipIds()));
        }
        if (context.getActive() != null) {
            specifications.add((root, query, builder) -> builder.equal(root.get("active"), context.getActive()));
        }
        var specification = specifications.stream()
                .reduce(Specification::and)
                .orElseGet(() -> (root, query, criteriaBuilder) -> null);
        return this.findAll(specification);
    }
}
