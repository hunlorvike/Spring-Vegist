package project.vegist.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleDTO {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotBlank(message = "Thumbnail can't be blank")
    private String thumbnail;

    @NotBlank(message = "Seo title can't be blank")
    private String seoTitle;

    @NotBlank(message = "Meta keys can't be blank")
    private String metaKeys;

    @NotBlank(message = "Meta desc can't be blank")
    private String metaDesc;

    @NotNull(message = "Creator id can't be null")
    private Long creatorId;

    private List<Long> tagIds;

}
