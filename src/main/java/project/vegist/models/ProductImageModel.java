package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageModel {
    private Long id;
    private Long productId;
    private String imagePath;

    public ProductImageModel(Long productId, String imagePath) {
        this.productId = productId;
        this.imagePath = imagePath;
    }
}
