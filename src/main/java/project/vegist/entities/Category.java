package project.vegist.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_id", columnList = "id"),
        @Index(name = "idx_categories_name", columnList = "name")
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true, foreignKey = @ForeignKey(name = "fk_category_categories"))
    private Category parent;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "meta_keys")
    private String metaKeys;

    @Column(name = "meta_desc")
    private String metaDesc;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Product> products;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}

