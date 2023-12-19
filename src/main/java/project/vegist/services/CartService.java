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

import java.time.LocalDateTime;
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

    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        User user = new User();
        user.setId(userId);
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}
