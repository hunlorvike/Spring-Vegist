package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import project.vegist.entities.HocSinh;

@Repository
public interface HocSinhRepository extends JpaRepository<HocSinh, Long>, JpaSpecificationExecutor<HocSinh> {
}
