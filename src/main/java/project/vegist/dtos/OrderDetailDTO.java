package project.vegist.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Product price cannot be null")
    private BigDecimal productPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
