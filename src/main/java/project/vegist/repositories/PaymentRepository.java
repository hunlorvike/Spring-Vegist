package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
