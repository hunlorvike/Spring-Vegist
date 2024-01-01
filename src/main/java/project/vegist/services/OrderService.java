package project.vegist.services;

import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.OrderDTO;
import project.vegist.entities.*;
import project.vegist.enums.CartStatus;
import project.vegist.enums.OrderStatus;
import project.vegist.models.OrderDetailModel;
import project.vegist.models.OrderModel;
import project.vegist.repositories.*;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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
    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CartService cartService, CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, CouponRepository couponRepository, PaymentRepository paymentRepository) {
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
        return orderRepository.findAll().stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public List<OrderModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable).getContent().stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public Optional<OrderModel> findById(Long id) {
        return orderRepository.findById(id).map(order -> {
            Hibernate.initialize(order.getOrderDetails());
            return order;
        }).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<OrderModel> create(OrderDTO orderDTO) throws IOException {
        // Check the user's cart with PENDING status
        Cart userCart = cartRepository.findByUserIdAndStatus(orderDTO.getUserId(), CartStatus.PENDING)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User's cart with PENDING status not found"));

        // Create a new order and populate it
        Order newOrder = new Order();
        convertToEntity(orderDTO, newOrder);

        // Create payment data
        Payment newPayment = new Payment();
        newPayment.setPaymentMethod(orderDTO.getPaymentMethod());
        newPayment.setAmount(calculateTotalAmount(userCart.getCartItems(), orderDTO.getShippingAmount()));
        newPayment.setStatus(orderDTO.getPaymentStatus());

        // Save and set the payment for the order
        newOrder.setPayment(paymentRepository.save(newPayment));

        // Save the order and flush
        newOrder = orderRepository.saveAndFlush(newOrder);

        // Save orderDetails and update userCart status
        List<OrderDetail> savedOrderDetails = saveOrderDetails(newOrder, userCart);
        newOrder.setOrderDetails(savedOrderDetails);
        userCart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(userCart);

        // Convert to OrderModel
        return Optional.ofNullable(convertToModel(newOrder));
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cartItems, BigDecimal shippingAmount) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            totalAmount = totalAmount.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        return totalAmount.add(shippingAmount);
    }

    @Transactional
    public List<OrderDetail> saveOrderDetails(Order order, Cart cart) {
        if (cart != null) {
            List<OrderDetail> orderDetails = cart.getCartItems().stream()
                    .map(cartItem -> convertToOrderDetailEntity(cartItem, order))
                    .collect(Collectors.toList());

            // Save orderDetails
            return orderDetailRepository.saveAll(orderDetails);
        } else {
            throw new IllegalArgumentException("Cart cannot be null");
        }
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
    @Transactional
    public List<OrderModel> createAll(List<OrderDTO> orderDTOS) throws IOException {
        List<OrderModel> createdOrders = new ArrayList<>();
        for (OrderDTO orderDTO : orderDTOS) {
            Optional<OrderModel> createdOrder = create(orderDTO);
            createdOrder.ifPresent(createdOrders::add);
        }
        return createdOrders;
    }

    @Override
    @Transactional
    public Optional<OrderModel> update(Long id, OrderDTO orderDTO) {
        return orderRepository.findById(id).map(existingOrder -> {
            convertToEntity(orderDTO, existingOrder);
            Order updatedOrder = orderRepository.save(existingOrder);
            return convertToModel(updatedOrder);
        });
    }

    @Override
    @Transactional
    public List<OrderModel> updateAll(Map<Long, OrderDTO> longOrderDTOMap) {
        List<OrderModel> updatedOrders = new ArrayList<>();
        for (Map.Entry<Long, OrderDTO> entry : longOrderDTOMap.entrySet()) {
            Long orderId = entry.getKey();
            OrderDTO orderDTO = entry.getValue();
            Optional<OrderModel> updatedOrder = update(orderId, orderDTO);
            updatedOrder.ifPresent(updatedOrders::add);
        }
        return updatedOrders;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Order> ordersToDelete = orderRepository.findAllById(ids);
        if (!ordersToDelete.isEmpty()) {
            orderRepository.deleteAll(ordersToDelete);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public List<OrderModel> search(String keywords) {
        SpecificationsBuilder<Order> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("user.username", keywords);
                        builder.like("orderStatus", keywords);
                        builder.like("coupon.code", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Order> spec = specificationsBuilder.build();

        return orderRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public OrderModel convertToModel(Order order) {
        List<OrderDetailModel> orderDetailModels = Collections.emptyList(); // Mặc định là danh sách rỗng

        if (order.getOrderDetails() != null) {
            Hibernate.initialize(order.getOrderDetails()); // Ensure orderDetails are loaded
            orderDetailModels = order.getOrderDetails().stream().map(this::convertOrderDetailToModel).collect(Collectors.toList());
        }

        return new OrderModel(order.getId(), order.getUser().getId(), order.getOrderStatus(), (order.getCoupon() != null) ? order.getCoupon().getId() : null, order.getShippingAmount(), (order.getPayment() != null) ? order.getPayment().getId() : null, orderDetailModels, DateTimeUtils.formatLocalDateTime(order.getCreatedAt()), DateTimeUtils.formatLocalDateTime(order.getUpdatedAt()));
    }

    private OrderDetailModel convertOrderDetailToModel(OrderDetail orderDetail) {
        return new OrderDetailModel(orderDetail.getId(), orderDetail.getOrder().getId(), orderDetail.getProduct().getId(), orderDetail.getProductPrice(), orderDetail.getQuantity(), DateTimeUtils.formatLocalDateTime(orderDetail.getCreatedAt()), DateTimeUtils.formatLocalDateTime(orderDetail.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(OrderDTO orderDTO, Order order) {
        order.setUser(userRepository.findById(orderDTO.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + orderDTO.getUserId())));

        order.setOrderStatus(OrderStatus.DELIVERING);

        if (orderDTO.getCouponId() != null) {
            order.setCoupon(couponRepository.findById(orderDTO.getCouponId()).orElseThrow(() -> new EntityNotFoundException("Coupon not found with ID: " + orderDTO.getCouponId())));
        }

        order.setShippingAmount(orderDTO.getShippingAmount());

    }

}
