package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.UserWishlistDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.UserWishlistModel;
import project.vegist.repositories.UserWishlistRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.UserWishlistService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class UserWishlistController {
    private final UserWishlistService userWishlistService;
    private final UserWishlistRepository userWishlistRepository;

    @Autowired
    public UserWishlistController(UserWishlistService userWishlistService, UserWishlistRepository userWishlistRepository) {
        this.userWishlistService = userWishlistService;
        this.userWishlistRepository = userWishlistRepository;
    }

    @GetMapping("/user-wishlists")
    public ResponseEntity<BaseResponse<List<UserWishlistModel>>> getAllUserWishlists(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<UserWishlistModel> wishlistModels = userWishlistService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(wishlistModels, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/user-wishlists/{id}")
    public ResponseEntity<BaseResponse<UserWishlistModel>> getUserWishlistById(@PathVariable Long id) {
        try {
            Optional<UserWishlistModel> wishlistModel = userWishlistService.findById(id);
            return wishlistModel.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "User wishlist not found", null)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/user-wishlists")
    public ResponseEntity<BaseResponse<UserWishlistModel>> createUserWishlist(@Valid @RequestBody UserWishlistDTO wishlistDTO) {
        try {
            Long userId = wishlistDTO.getUserId();
            Long productId = wishlistDTO.getProductId();

            // Check if the wishlist item already exists for the given user and product
            if (userWishlistRepository.existsByUserIdAndProductId(userId, productId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse<>("Product already exists in user's wishlist"));
            }

            // Continue with creating the wishlist item
            Optional<UserWishlistModel> createdWishlist = userWishlistService.create(wishlistDTO);
            return createdWishlist.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create user wishlist"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }


    @PutMapping("/user-wishlists/{id}")
    public ResponseEntity<BaseResponse<UserWishlistModel>> updateUserWishlist(@PathVariable Long id, @Valid @RequestBody UserWishlistDTO wishlistDTO) {
        try {
            Optional<UserWishlistModel> updatedUserWishlist = userWishlistService.update(id, wishlistDTO);
            return updatedUserWishlist.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("User wishlist not found"))));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/user-wishlists/{id}")
    public ResponseEntity<BaseResponse<String>> deleteUserWishlist(@PathVariable Long id) {
        try {
            boolean deleted = userWishlistService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("User wishlist deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("User wishlist not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}

