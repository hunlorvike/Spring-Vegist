package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import project.vegist.entities.UserWishlist;

@Repository
public interface UserWishlistRepository extends JpaRepository<UserWishlist, Long>, JpaSpecificationExecutor<UserWishlist> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
