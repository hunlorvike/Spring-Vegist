package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Unit;
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    boolean existsByUnitValueAndUnitName(Integer value, String name);

    boolean existsByUnitValue(Integer unitValue);
}
