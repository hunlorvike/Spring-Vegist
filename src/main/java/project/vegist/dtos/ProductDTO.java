package project.vegist.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name must be less than or equal to 255 characters")
    private String productName;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than or equal to 0")
    private BigDecimal salePrice;

    @NotBlank(message = "SKU cannot be blank")
    private String SKU;

    @NotNull(message = "Product thumbnail cannot be null")
    private MultipartFile thumbnail;

    @Min(value = 0, message = "View count must be greater than or equal to 0")
    private Integer viewCount;

    @Min(value = 0, message = "Wishlist count must be greater than or equal to 0")
    private Integer wishlistCount;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "Label ID cannot be null")
    private Long labelId;

    @Min(value = 0, message = "Discount must be greater than or equal to 0")
    private Integer discount;

    private String iframeVideo;

    @Size(max = 255, message = "SEO title must be less than or equal to 255 characters")
    private String seoTitle;

    @Size(max = 255, message = "Meta keys must be less than or equal to 255 characters")
    private String metaKeys;

    @Size(max = 255, message = "Meta description must be less than or equal to 255 characters")
    private String metaDesc;

    private List<MultipartFile> imagesProduct;

    public void setImagesProduct(MultipartFile[] imagesProduct) {
        this.imagesProduct = Arrays.asList(imagesProduct);
    }
}
