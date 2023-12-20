package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductModel {
    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String SKU;
    private String thumbnail;
    private String iframeVideo;
    private Integer viewCount;
    private Integer wishlistCount;
    private Long categoryId;
    private Long labelId;
    private Integer discount; // % giảm giá
    private String seoTitle;
    private String metaKeys;
    private String metaDesc;
    private String createdAt;
    private String updatedAt;
    private List<ProductImageModel> productImages;
    private List<ProductUnitModel> productUnits;

}
