package project.vegist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.entities.CartItem;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.CartService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user-cart-items")
    public ResponseEntity<BaseResponse<List<CartItem>>> getUserCartItems(
            @RequestParam(name = "userId") Long userId) {
        try {
            List<CartItem> cartItemModels = cartService.getCartItems(userId);
            return ResponseEntity.ok(new SuccessResponse<>(cartItemModels, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }


    @PostMapping("/add-to-cart")
    public ResponseEntity<BaseResponse<String>> addToCart(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "productId") Long productId,
            @RequestParam(name = "quantity", defaultValue = "1") Integer quantity) {
        try {
            cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(new SuccessResponse<>("Sản phẩm đã được thêm vào giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/update-cart-item")
    public ResponseEntity<BaseResponse<String>> updateCartItem(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "itemId") Long itemId,
            @RequestParam(name = "quantity") Integer quantity) {
        try {
            cartService.updateCartItemQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(new SuccessResponse<>("Số lượng sản phẩm trong giỏ hàng đã được cập nhật"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/remove-from-cart")
    public ResponseEntity<BaseResponse<String>> removeFromCart(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "itemId") Long itemId) {
        try {
            cartService.deleteCartItem(userId, itemId);
            return ResponseEntity.ok(new SuccessResponse<>("Sản phẩm đã được xóa khỏi giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/user-cart-total")
    public ResponseEntity<BaseResponse<BigDecimal>> getUserCartTotal(
            @RequestParam(name = "userId") Long userId) {
        try {
            BigDecimal cartTotal = cartService.calculateCartTotal(userId);
            return ResponseEntity.ok(new SuccessResponse<>(cartTotal, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/clear-cart")
    public ResponseEntity<BaseResponse<String>> clearUserCart(
            @RequestParam(name = "userId") Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(new SuccessResponse<>("Giỏ hàng đã được xóa sạch"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/apply-discount-to-cart")
    public ResponseEntity<BaseResponse<String>> applyDiscountToUserCart(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "discountRate") BigDecimal discountRate) {
        try {
            cartService.applyDiscountToCart(userId, discountRate);
            return ResponseEntity.ok(new SuccessResponse<>("Giảm giá đã được áp dụng cho giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/apply-discount-to-cart-item")
    public ResponseEntity<BaseResponse<String>> applyDiscountToCartItem(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "itemId") Long itemId,
            @RequestParam(name = "discountRate") BigDecimal discountRate) {
        try {
            cartService.applyDiscountToCartItem(userId, itemId, discountRate);
            return ResponseEntity.ok(new SuccessResponse<>("Giảm giá đã được áp dụng cho sản phẩm trong giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
