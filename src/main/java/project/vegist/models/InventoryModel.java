package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryModel {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String createdAt;
    private String updatedAt;
}

