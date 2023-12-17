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
    private List<ReviewModel> productReviews;
    private List<ProductUnitModel> productUnits;
    private List<InventoryModel> inventories;
    private List<UserWishlistModel> userWishlists;
    private List<CartItemModel> cartItems;
    private List<OrderDetailModel> orderDetails;

    public ProductModel(Long id, String productName, String description, BigDecimal price, BigDecimal salePrice,
                        String SKU, String thumbnail, String iframeVideo, Integer viewCount, Integer wishlistCount,
                        Long categoryId, Long labelId, Integer discount, String seoTitle, String metaKeys, String metaDesc,
                        String createdAt, String updatedAt, List<ProductImageModel> productImages) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.SKU = SKU;
        this.thumbnail = thumbnail;
        this.iframeVideo = iframeVideo;
        this.viewCount = viewCount;
        this.wishlistCount = wishlistCount;
        this.categoryId = categoryId;
        this.labelId = labelId;
        this.discount = discount;
        this.seoTitle = seoTitle;
        this.metaKeys = metaKeys;
        this.metaDesc = metaDesc;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.productImages = productImages;
    }
}
