package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.CartDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.CartModel;
import project.vegist.repositories.CartItemRepository;
import project.vegist.repositories.CartRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.CartService;
import project.vegist.services.JwtService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public CartController(CartService cartService, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository
            , JwtService jwtService) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/carts")
    public ResponseEntity<BaseResponse<List<CartModel>>> getAllCarts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<CartModel> cartModels = cartService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(cartModels, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/carts/{id}")
    public ResponseEntity<BaseResponse<CartModel>> getCartById(@PathVariable Long id) {
        try {
            Optional<CartModel> cartModel = cartService.findById(id);
            return cartModel.map(value -> ResponseEntity.ok((BaseResponse<CartModel>) new SuccessResponse<>(value, null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Cart not found", null)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/my-pending-cart")
    public ResponseEntity<BaseResponse<CartModel>> getMyPendingCart(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            Optional<CartModel> cartModel = cartService.findPendingCartByUserId(userId);
            return cartModel.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Pending cart not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    private Long getUserIdFromToken(String token) {
        return jwtService.getUserIdFromToken(token);
    }

    @PostMapping("/carts")
    public ResponseEntity<BaseResponse<CartModel>> createCart(@Valid @RequestBody CartDTO cartDTO) {
        try {
            Optional<CartModel> createdCart = cartService.create(cartDTO);
            return createdCart.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create cart"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/carts/{id}")
    public ResponseEntity<BaseResponse<CartModel>> updateCart(@PathVariable Long id, @Valid @RequestBody CartDTO cartDTO) {
        try {
            Optional<CartModel> updatedCart = cartService.update(id, cartDTO);
            return updatedCart.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Cart not found"))));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }


    @DeleteMapping("/carts/{id}")
    public ResponseEntity<BaseResponse<String>> deleteCart(@PathVariable Long id) {
        try {
            boolean deleted = cartService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Cart deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Cart not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/carts")
    public ResponseEntity<BaseResponse<String>> deleteCarts(@RequestBody List<Long> cartIds) {
        try {
            boolean deleted = cartService.deleteAll(cartIds);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Carts deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Carts not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
