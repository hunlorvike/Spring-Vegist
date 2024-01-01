package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.entities.CartItem;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>, JpaSpecificationExecutor<CartItem> {
    void deleteByCartId(Long id);

    List<CartItem> findByCartIdIn(List<Long> ids);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM cart_items WHERE cart_id = :cartId", nativeQuery = true)
    int deleteCartItemsByCartId(Long cartId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_items WHERE cart_id IN (:ids)", nativeQuery = true)
    int deleteCartItemsByCartIds(List<Long> ids);
}
