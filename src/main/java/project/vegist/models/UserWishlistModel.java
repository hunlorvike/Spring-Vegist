package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.entities.Product;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWishlistModel {
    private Long id;
    private Long userId;
    private Long productId;
//    private Product product;
    private String createdAt;
    private String updatedAt;
}
