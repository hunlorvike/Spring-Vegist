package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.entities.Cart;
import project.vegist.entities.CartItem;
import project.vegist.entities.Product;
import project.vegist.entities.User;
import project.vegist.enums.CartStatus;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.repositories.CartRepository;
import project.vegist.repositories.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    public void addToCart(Long userId, Long productId, Integer quantity) {
        // Kiểm tra xem người dùng đã có giỏ hàng chưa
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.PENDING)

                .orElseGet(() -> createCart(userId));

        // Lấy thông tin sản phẩm từ database
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId, HttpStatus.NOT_FOUND));


        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice());
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    public void deleteCartItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", userId, HttpStatus.NOT_FOUND));

        // Find the cart item by ID
        Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cart.getCartItems().remove(cartItem);
            cartRepository.save(cart);
        } else {
            throw new ResourceNotFoundException("CartItem", itemId, HttpStatus.NOT_FOUND);
        }
    }

    public void updateCartItemQuantity(Long userId, Long itemId, Integer newQuantity) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", userId, HttpStatus.NOT_FOUND));

        // Find the cart item by ID
        Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            if (newQuantity > 0) {
                cartItem.setQuantity(newQuantity);
                cartRepository.save(cart);
            } else {
                throw new IllegalArgumentException("New quantity should be greater than 0");
            }
        } else {
            throw new ResourceNotFoundException("CartItem", itemId, HttpStatus.NOT_FOUND);
        }
    }

    public Cart getUserCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", userId, HttpStatus.NOT_FOUND));
    }


    public List<CartItem> getCartItems(Long userId) {
        Cart cart = getUserCart(userId);
        return cart.getCartItems();
    }

    public BigDecimal calculateCartTotal(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        return cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart(Long userId) {
        Cart cart = getUserCart(userId);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public void updateCartStatus(Long userId, CartStatus newStatus) {
        Cart cart = getUserCart(userId);
        cart.setStatus(newStatus);
        cartRepository.save(cart);
    }

    public BigDecimal calculateCartItemTotal(Long userId, Long itemId) {
        Cart cart = getUserCart(userId);
        CartItem cartItem = findCartItemById(cart, itemId);
        return cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    public BigDecimal calculateCartTotalWithDiscount(Long userId, BigDecimal discountRate) {
        BigDecimal cartTotal = calculateCartTotal(userId);
        BigDecimal discountAmount = cartTotal.multiply(discountRate);
        return cartTotal.subtract(discountAmount);
    }

    public void applyDiscountToCartItem(Long userId, Long itemId, BigDecimal discountRate) {
        Cart cart = getUserCart(userId);
        CartItem cartItem = findCartItemById(cart, itemId);
        BigDecimal discountedPrice = cartItem.getPrice().multiply(BigDecimal.ONE.subtract(discountRate));
        cartItem.setPrice(discountedPrice);
        cartRepository.save(cart);
    }

    public void applyDiscountToCart(Long userId, BigDecimal discountRate) {
        Cart cart = getUserCart(userId);
        List<CartItem> cartItems = cart.getCartItems();

        for (CartItem cartItem : cartItems) {
            BigDecimal discountedPrice = cartItem.getPrice().multiply(BigDecimal.ONE.subtract(discountRate));
            cartItem.setPrice(discountedPrice);
        }

        cartRepository.save(cart);
    }

    private CartItem findCartItemById(Cart cart, Long itemId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId, HttpStatus.NOT_FOUND));
    }


    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        User user = new User();
        user.setId(userId);
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}
