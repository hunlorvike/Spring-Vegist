package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.CouponDTO;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.CouponModel;
import project.vegist.repositories.CouponRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.CouponService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class CouponController {
    private final CouponService couponService;
    private final CouponRepository couponRepository;

    @Autowired
    public CouponController(CouponService couponService, CouponRepository couponRepository) {
        this.couponService = couponService;
        this.couponRepository = couponRepository;
    }

    @GetMapping("/coupons")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<CouponModel>>> getAllCoupons(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            List<CouponModel> coupons = couponService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(coupons));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/coupons/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<CouponModel>> getCouponById(@PathVariable Long id) {
        try {
            Optional<CouponModel> coupon = couponService.findById(id);
            return coupon.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Coupon not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/coupons")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<CouponModel>> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        try {
            if (couponRepository.existsByValue(couponDTO.getValue())) {
                throw new ResourceExistException("coupon with value " + couponDTO.getValue(), HttpStatus.CONFLICT);
            }

            Optional<CouponModel> createdCoupon = couponService.create(couponDTO);
            return createdCoupon.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create coupon"))));
        } catch (ResourceExistException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<CouponModel>> updateCoupon(@PathVariable Long id, @RequestBody CouponDTO couponDTO) {
        try {
            Optional<CouponModel> updatedCoupon = couponService.update(id, couponDTO);
            return updatedCoupon.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Coupon not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteCoupon(@PathVariable Long id) {
        try {
            boolean deleted = couponService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Coupon deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Coupon not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

}
