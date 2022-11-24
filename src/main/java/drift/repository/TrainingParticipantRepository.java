package drift.repository;

import drift.model.TrainingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TrainingParticipantRepository extends JpaRepository<TrainingParticipant, String> {

    boolean existsByTrainingIdAndUserId(String trainingId, String userId);

    void deleteByTrainingIdAndUserId(String trainingId, String userId);

    Collection<TrainingParticipant> findAllByTrainingId(String trainingId);
}
