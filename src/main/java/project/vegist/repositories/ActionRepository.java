package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Action;
@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    boolean existsByActionName(String name);
}
