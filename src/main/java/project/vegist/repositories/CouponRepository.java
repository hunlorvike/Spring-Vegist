package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Coupon;
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByValue(String name);
}
