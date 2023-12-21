package project.vegist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.dtos.ProductDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ProductModel;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.ProductService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<ProductModel>>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            List<ProductModel> products = productService.findAll(page, size);
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
    public ResponseEntity<BaseResponse<ProductModel>> createProduct(
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "salePrice", required = false) BigDecimal salePrice,
            @RequestParam("SKU") String SKU,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "imagesProduct", required = false) MultipartFile[] imagesProduct,
            @RequestParam(value = "viewCount", defaultValue = "0") Integer viewCount,
            @RequestParam(value = "wishLishCount", defaultValue = "0") Integer wishLishCount,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("labelId") Long labelId,
            @RequestParam(value = "discount", defaultValue = "0") Integer discount,
            @RequestParam(value = "iframeVideo", required = false) String iframeVideo,
            @RequestParam(value = "seoTitle", required = false) String seoTitle,
            @RequestParam(value = "metaKeys", required = false) String metaKeys,
            @RequestParam(value = "metaDesc", required = false) String metaDesc,
            @RequestParam(value = "unitIds", required = false) List<Long> unitIds) {
        if (thumbnail != null || imagesProduct != null) {
            try {
                ProductDTO productDTO = new ProductDTO(productName, description, price, salePrice, SKU,
                        thumbnail, viewCount, wishLishCount, categoryId, labelId, discount, iframeVideo,
                        seoTitle, metaKeys, metaDesc, unitIds, imagesProduct
                );
                Optional<ProductModel> createdProduct = productService.create(productDTO);
                return createdProduct.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse<>(Collections.singletonList("Failed to create product"))));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
            }
        } else {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse<>(Collections.singletonList("Thumbnail or imagesProduct is null")));
        }
    }


    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductModel>> updateProduct(
            @PathVariable Long id,
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "salePrice", required = false) BigDecimal salePrice,
            @RequestParam("SKU") String SKU,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "imagesProduct", required = false) MultipartFile[] imagesProduct,
            @RequestParam(value = "viewCount", defaultValue = "0") Integer viewCount,
            @RequestParam(value = "wishLishCount", defaultValue = "0") Integer wishLishCount,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("labelId") Long labelId,
            @RequestParam(value = "discount", defaultValue = "0") Integer discount,
            @RequestParam(value = "iframeVideo", required = false) String iframeVideo,
            @RequestParam(value = "seoTitle", required = false) String seoTitle,
            @RequestParam(value = "metaKeys", required = false) String metaKeys,
            @RequestParam(value = "metaDesc", required = false) String metaDesc,
            @RequestParam(value = "unitIds", required = false) List<Long> unitIds) {
        try {
            ProductDTO productDTO = new ProductDTO(productName, description, price, salePrice, SKU,
                    thumbnail, viewCount, wishLishCount, categoryId, labelId, discount, iframeVideo,
                    seoTitle, metaKeys, metaDesc, unitIds, imagesProduct
            );

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
            boolean deleted = productService.deleteById(id);
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

