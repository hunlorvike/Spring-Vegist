package project.vegist.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}

