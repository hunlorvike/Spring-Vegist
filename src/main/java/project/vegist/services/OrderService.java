package project.vegist.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.OrderDTO;
import project.vegist.entities.Cart;
import project.vegist.entities.CartItem;
import project.vegist.entities.Order;
import project.vegist.entities.OrderDetail;
import project.vegist.enums.CartStatus;
import project.vegist.enums.OrderStatus;
import project.vegist.models.OrderDetailModel;
import project.vegist.models.OrderModel;
import project.vegist.repositories.*;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService implements CrudService<Order, OrderDTO, OrderModel> {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CartService cartService, CartRepository cartRepository,
                        CartItemRepository cartItemRepository, UserRepository userRepository, CouponRepository couponRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
    }


    @Override
    public List<OrderModel> findAll() {
        return orderRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderModel> findById(Long id) {
        return orderRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<OrderModel> create(OrderDTO orderDTO) throws IOException {
        // Kiểm tra giỏ hàng có trạng thái PENDING của người dùng
        Cart userCart = cartRepository.findByUserIdAndStatus(orderDTO.getUserId(), CartStatus.PENDING)
                .orElseThrow(() -> new EntityNotFoundException("User's cart with PENDING status not found"));

        Order newOrder = new Order();
        convertToEntity(orderDTO, newOrder);
        // Lưu thông tin order và orderDetails
        newOrder = orderRepository.save(newOrder);

        saveOrderDetails(newOrder, userCart.getCartItems());

        // Cập nhật trạng thái giỏ hàng sau khi đã tạo order
        userCart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(userCart);

        return Optional.ofNullable(convertToModel(newOrder));
    }

    private void saveOrderDetails(Order order, List<CartItem> cartItems) {
        List<OrderDetail> orderDetails = cartItems.stream()
                .map(cartItem -> convertToOrderDetailEntity(cartItem, order))
                .collect(Collectors.toList());

        // Lưu thông tin orderDetails
        orderDetailRepository.saveAll(orderDetails);
    }

    private OrderDetail convertToOrderDetailEntity(CartItem cartItem, Order order) {
        OrderDetail orderDetail = new OrderDetail();
        // Cài đặt các giá trị cho orderDetail từ cartItem và order
        orderDetail.setOrder(order);
        orderDetail.setProduct(cartItem.getProduct());
        orderDetail.setProductPrice(cartItem.getPrice());
        orderDetail.setQuantity(cartItem.getQuantity());

        return orderDetail;
    }


    @Override
    public List<OrderModel> createAll(List<OrderDTO> orderDTOS) throws IOException {
        return null;
    }

    @Override
    public Optional<OrderModel> update(Long id, OrderDTO orderDTO) {
        return Optional.empty();
    }

    @Override
    public List<OrderModel> updateAll(Map<Long, OrderDTO> longOrderDTOMap) {
        return null;
    }

    @Override
    public boolean deleteById(Long id) {
        return false;
    }

    @Override
    public boolean deleteAll(List<Long> ids) {
        return false;
    }

    @Override
    public List<OrderModel> search(String keywords) {
        return null;
    }

    @Override
    public OrderModel convertToModel(Order order) {
        List<OrderDetailModel> orderDetailModels = order.getOrderDetails().stream()
                .map(this::convertOrderDetailToModel)
                .collect(Collectors.toList());

        return new OrderModel(
                order.getId(),
                order.getUser().getId(),
                order.getOrderStatus(),
                order.getCoupon().getId(),
                order.getShippingAmount(),
                order.getPayment().getId(),
                orderDetailModels,
                DateTimeUtils.formatLocalDateTime(order.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(order.getUpdatedAt())
        );
    }

    private OrderDetailModel convertOrderDetailToModel(OrderDetail orderDetail) {
        return new OrderDetailModel(
                orderDetail.getId(),
                orderDetail.getOrder().getId(),
                orderDetail.getProduct().getId(),
                orderDetail.getProductPrice(),
                orderDetail.getQuantity(),
                DateTimeUtils.formatLocalDateTime(orderDetail.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(orderDetail.getUpdatedAt())
        );
    }

    @Override
    public void convertToEntity(OrderDTO orderDTO, Order order) {
        order.setUser(userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + orderDTO.getUserId())));

        order.setOrderStatus(OrderStatus.DELIVERING);

        if (orderDTO.getCouponId() != null) {
            order.setCoupon(couponRepository.findById(orderDTO.getCouponId())
                    .orElseThrow(() -> new EntityNotFoundException("Coupon not found with ID: " + orderDTO.getCouponId())));
        }

        order.setShippingAmount(orderDTO.getShippingAmount());

        if (orderDTO.getPaymentId() != null) {
            order.setPayment(paymentRepository.findById(orderDTO.getPaymentId())
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found with ID: " + orderDTO.getPaymentId())));

        }
    }

}
