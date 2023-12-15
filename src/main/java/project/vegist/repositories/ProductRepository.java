package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Product;
import project.vegist.models.ProductModel;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
