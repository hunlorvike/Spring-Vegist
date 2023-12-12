package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    boolean existsByLabelName(String name);
}
