package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWishlistModel {
    private Long id;
    private Long userId;
    private Long productId;
    private String createdAt;
    private String updatedAt;
}
