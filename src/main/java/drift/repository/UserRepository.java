package drift.repository;

import drift.model.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActive(String email, boolean active);

    Optional<User> findByIdAndActive(String id, boolean active);

    default Collection<User> search(UsersSearchContext context) {
        var specifications = new ArrayList<Specification<User>>();
        if (CollectionUtils.isNotEmpty(context.getUserIds())) {
            specifications.add((root, query, builder) -> root.get("id").in(context.getUserIds()));
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
