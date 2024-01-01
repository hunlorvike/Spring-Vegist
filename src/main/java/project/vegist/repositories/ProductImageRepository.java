package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.entities.ProductImage;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long>, JpaSpecificationExecutor<ProductImage> {
    List<ProductImage> findByProduct_Id(Long productId);

    @Transactional
    @Modifying
    void deleteByProductIdAndImagePathIn(Long productId, List<String> imagePaths);

    void deleteByProduct_IdAndImagePath(Long id, String imagePath);
}
