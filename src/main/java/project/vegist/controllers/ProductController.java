package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.ProductDTO;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ProductModel;
import project.vegist.repositories.ProductRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.ProductService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<ProductModel>>> getAllProducts() {
        try {
            List<ProductModel> products = productService.findAll();
            return ResponseEntity.ok(new SuccessResponse<>(products));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductModel>> getProductById(@PathVariable Long id) {
        try {
            Optional<ProductModel> product = productService.findById(id);
            return product.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Product not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductModel>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            if (productRepository.existsByProductName(productDTO.getProductName())) {
                throw new ResourceExistException("product " + productDTO.getProductName(), HttpStatus.CONFLICT);
            }

            Optional<ProductModel> createdProduct = productService.create(productDTO);
            return createdProduct.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create product"))));
        } catch (ResourceExistException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductModel>> updateProduct(@PathVariable Long id,
                                                                    @RequestBody ProductDTO productDTO) {
        try {
            Optional<ProductModel> updatedProduct = productService.update(id, productDTO);
            return updatedProduct.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Product not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleleById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Product deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Product not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}

