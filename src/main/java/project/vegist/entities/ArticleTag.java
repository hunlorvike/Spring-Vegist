package project.vegist.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "articles_tags", uniqueConstraints = {
        @UniqueConstraint(name = "unique_articles_tags", columnNames = {"articles_id", "tag_id"})
})
public class ArticleTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "articles_id", nullable = false, foreignKey = @ForeignKey(name = "fk_articles_tags_articles"))
    private Articles articles;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false, foreignKey = @ForeignKey(name = "fk_articles_tags_tags"))
    private Tag tag;

}
