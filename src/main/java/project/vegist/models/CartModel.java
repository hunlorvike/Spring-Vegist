package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.CartStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartModel {
    private Long id;
    private Long userId;
    private List<CartItemModel> cartItems;
    private CartStatus status;
    private String createdAt;
    private String updatedAt;
}

