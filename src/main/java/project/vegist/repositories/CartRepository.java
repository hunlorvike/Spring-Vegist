package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}
