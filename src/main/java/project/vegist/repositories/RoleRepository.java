package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import project.vegist.entities.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String name);

    boolean existsByRoleName(String name);
}
