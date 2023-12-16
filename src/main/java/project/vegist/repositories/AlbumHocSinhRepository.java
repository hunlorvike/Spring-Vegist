package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.AlbumHocSinh;

@Repository
public interface AlbumHocSinhRepository extends JpaRepository<AlbumHocSinh, Long> {
}
