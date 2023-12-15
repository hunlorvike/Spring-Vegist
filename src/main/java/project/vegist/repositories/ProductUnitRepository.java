package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.ProductUnit;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
}
