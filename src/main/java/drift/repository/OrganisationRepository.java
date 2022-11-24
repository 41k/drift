package drift.repository;

import drift.model.Organisation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.Collection;

public interface OrganisationRepository extends JpaRepository<Organisation, String>, JpaSpecificationExecutor<Organisation> {

    boolean existsByOwnerIdAndActive(String ownerId, boolean active);

    default Collection<Organisation> search(OrganisationsSearchContext context) {
        var specifications = new ArrayList<Specification<Organisation>>();
        if (CollectionUtils.isNotEmpty(context.getOrganisationIds())) {
            specifications.add((root, query, builder) -> root.get("id").in(context.getOrganisationIds()));
        }
        if (CollectionUtils.isNotEmpty(context.getOwnerIds())) {
            specifications.add((root, query, builder) -> root.get("ownerId").in(context.getOwnerIds()));
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
