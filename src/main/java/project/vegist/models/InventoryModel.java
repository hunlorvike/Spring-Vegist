package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.entities.Product;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryModel {
    private Long id;
    private Long productId;
    private Product product;
    private Integer quantity;
    private String createdAt;
    private String updatedAt;

    public InventoryModel(Long id, Long productId, Integer quantity, String createdAt, String updatedAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

