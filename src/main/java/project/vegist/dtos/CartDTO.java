package project.vegist.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Valid
    private List<CartItemDTO> cartItems;

}
/*
{
  "userId": 1,
  "cartItems": [
    {
      "productId": 101,
      "quantity": 2,
      "price": 19.99
    },
    {
      "productId": 102,
      "quantity": 1,
      "price": 29.99
    }
  ],
  "status": "PENDING"
}
*/
