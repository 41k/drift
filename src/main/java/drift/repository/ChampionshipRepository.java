package drift.repository;

import drift.model.Championship;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.Collection;

public interface ChampionshipRepository extends JpaRepository<Championship, String>, JpaSpecificationExecutor<Championship> {

    default Collection<Championship> search(ChampionshipsSearchContext context) {
        var specifications = new ArrayList<Specification<Championship>>();
        if (CollectionUtils.isNotEmpty(context.getChampionshipIds())) {
            specifications.add((root, query, builder) -> root.get("id").in(context.getChampionshipIds()));
        }
        if (CollectionUtils.isNotEmpty(context.getOwnerIds())) {
            specifications.add((root, query, builder) -> root.get("ownerId").in(context.getOwnerIds()));
        }
        if (CollectionUtils.isNotEmpty(context.getOrganisationIds())) {
            specifications.add((root, query, builder) -> root.get("organisationId").in(context.getOrganisationIds()));
        }
        if (CollectionUtils.isNotEmpty(context.getDisciplines())) {
            specifications.add((root, query, builder) -> root.get("discipline").in(context.getDisciplines()));
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
