package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailModel {
    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal productPrice;
    private Integer quantity;
    private String createdAt;
    private String updatedAt;
}
