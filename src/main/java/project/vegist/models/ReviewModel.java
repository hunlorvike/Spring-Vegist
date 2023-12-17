package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.entities.Product;
import project.vegist.entities.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModel {
    private Long id;
    private Long userId;
    private User user;
    private Long productId;
    private Product product;
    private Integer rating;
    private String createdAt;
    private String updatedAt;
}
