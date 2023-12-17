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
}

