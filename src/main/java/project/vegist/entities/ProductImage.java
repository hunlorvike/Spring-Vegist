package project.vegist.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.utils.FileUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_images_product"))
    private Product product;

    @Column(name = "image_path", length = 255, nullable = false)
    private String imagePath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public ProductImage(Product product, String imagePath) {
        this.product = product;
        this.imagePath = imagePath;
    }

    public ProductImage(Product product, MultipartFile file) {
        this.product = product;
        try {
            this.imagePath = FileUtils.generateUniqueFileName(file.getOriginalFilename());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileName() {
        return FileUtils.getOriginalFileNameFromUrl(imagePath);
    }
}
