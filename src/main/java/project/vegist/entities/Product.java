package project.vegist.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_id", columnList = "id"),
        @Index(name = "idx_products_product_name", columnList = "product_name"),
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_label_id", columnList = "label_id")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    private String SKU;

    @Column(name = "thumbnail", length = 255)
    private String thumbnail;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "wishlist_count")
    private Integer wishlistCount;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_categories"))
    private Category category;

    @ManyToOne
    @JoinColumn(name = "label_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_labels"))
    private Label label;

    private Integer discount;

    @Column(name = "iframe_video", columnDefinition = "TEXT")
    private String iframeVideo;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "meta_keys", length = 255)
    private String metaKeys;

    @Column(name = "meta_desc", length = 255)
    private String metaDesc;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductImage> productImages;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Review> productReviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductUnit> productUnits;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserWishlist> userWishlists;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderDetail> orderDetails;
}

