package project.vegist.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.PaymentMethod;
import project.vegist.enums.Status;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private Long couponId;

    @NotNull(message = "Shipping amount cannot be null")
    @Positive(message = "Shipping amount must be positive")
    private BigDecimal shippingAmount;

    private PaymentMethod paymentMethod; // e.g., CREDIT_CARD, MOMO, CASH
    private Status paymentStatus; // e.g., pending, success, failure, etc.
}
