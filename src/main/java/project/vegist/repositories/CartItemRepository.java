package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
