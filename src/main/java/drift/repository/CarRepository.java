package drift.repository;

import drift.model.Car;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.Collection;

public interface CarRepository extends JpaRepository<Car, String>, JpaSpecificationExecutor<Car> {

    boolean existsByOwnerIdAndActive(String ownerId, boolean active);

    default Collection<Car> search(CarsSearchContext context) {
        var specifications = new ArrayList<Specification<Car>>();
        if (CollectionUtils.isNotEmpty(context.getCarIds())) {
            specifications.add((root, query, builder) -> root.get("id").in(context.getCarIds()));
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
