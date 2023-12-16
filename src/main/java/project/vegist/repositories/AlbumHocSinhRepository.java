package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.vegist.entities.AlbumHocSinh;

import java.util.List;

@Repository
public interface AlbumHocSinhRepository extends JpaRepository<AlbumHocSinh, Long> {
    List<AlbumHocSinh> findByHocsinh_Id(Long hocsinhId);

}
