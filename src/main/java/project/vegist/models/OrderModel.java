package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderModel {
    private Long id;
    private Long userId;
    private OrderStatus orderStatus;
    private Long couponId;
    private BigDecimal shippingAmount;
    private Long paymentId;
    private List<OrderDetailModel> orderDetails;
    private String createdAt; // Có thể sử dụng định dạng thời gian phù hợp
    private String updatedAt; // Có thể sử dụng định dạng thời gian phù hợp
}
