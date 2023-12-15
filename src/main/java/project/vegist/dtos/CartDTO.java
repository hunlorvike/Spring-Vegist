package project.vegist.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.CartStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Valid
    private List<CartItemDTO> cartItems;

    @NotNull(message = "Cart status cannot be null")
    private CartStatus status;
}

