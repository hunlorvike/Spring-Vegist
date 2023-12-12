package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.UserAction;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    boolean existsByUserIdAndActionId(Long userId, Long actionId);
}
