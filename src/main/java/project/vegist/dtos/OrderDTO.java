package project.vegist.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus orderStatus;

    // Assuming couponId can be null
    private Long couponId;

    @NotNull(message = "Shipping amount cannot be null")
    @Positive(message = "Shipping amount must be positive")
    private BigDecimal shippingAmount;

    @NotNull(message = "Payment ID cannot be null")
    private Long paymentId;
}