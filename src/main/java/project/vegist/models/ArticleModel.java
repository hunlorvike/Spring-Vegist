package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleModel {
    private Long id;
    private String title;
    private String content;
    private String thumbnail;
    private String seoTitle;
    private String metaKeys;
    private String metaDesc;
    private Long creatorId;
    private List<Long> tagIds;
    private String createdAt;
    private String updatedAt;
}
