package project.vegist.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 255, message = "Category name must be less than or equal to 255 characters")
    private String name;

    private Boolean status;

    private Long parentId;

    @Size(max = 255, message = "SEO title must be less than or equal to 255 characters")
    private String seoTitle;

    @Size(max = 255, message = "Meta keys must be less than or equal to 255 characters")
    private String metaKeys;

    @Size(max = 255, message = "Meta description must be less than or equal to 255 characters")
    private String metaDesc;
}
