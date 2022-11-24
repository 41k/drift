package drift.repository;

import drift.model.ScoringSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ScoringSystemRepository extends JpaRepository<ScoringSystem, String> {

    Collection<ScoringSystem> findAllByActive(boolean active);
}
