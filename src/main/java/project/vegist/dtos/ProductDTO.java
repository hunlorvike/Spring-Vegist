package project.vegist.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @NotNull(message = "Unit IDs cannot be null")
    @Size(min = 0, message = "Unit IDs must not be empty")
    private List<Long> unitIds;

    private List<MultipartFile> imagesProduct;

    public ProductDTO(String productName, String description, BigDecimal price, BigDecimal salePrice, String SKU,
                      MultipartFile thumbnail, Integer viewCount, Integer wishlistCount, Long categoryId, Long labelId,
                      Integer discount, String iframeVideo, String seoTitle, String metaKeys, String metaDesc,
                      List<Long> unitIds, MultipartFile[] imagesProduct) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.SKU = SKU;
        this.thumbnail = thumbnail;
        this.viewCount = viewCount;
        this.wishlistCount = wishlistCount;
        this.categoryId = categoryId;
        this.labelId = labelId;
        this.discount = discount;
        this.iframeVideo = iframeVideo;
        this.seoTitle = seoTitle;
        this.metaKeys = metaKeys;
        this.metaDesc = metaDesc;
        this.unitIds = (unitIds != null) ? unitIds : new ArrayList<>();
        // Convert the array to a mutable list
        this.imagesProduct = (imagesProduct != null) ? new ArrayList<>(Arrays.asList(imagesProduct)) : new ArrayList<>();
    }
}
