package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.entities.Cart;
import project.vegist.enums.CartStatus;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
    Optional<Cart> findByUserIdAndStatus(Long user_id, CartStatus status);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM carts WHERE id = :cartId", nativeQuery = true)
    int deleteCartById(Long cartId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM carts WHERE id IN (:ids)", nativeQuery = true)
    int deleteAllCartById(Iterable<? extends Long> ids);

}
