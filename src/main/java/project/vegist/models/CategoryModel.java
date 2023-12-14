package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModel {
    private Long id;
    private String name;
    private Boolean status;
    private Long parentId;
    private String seoTitle;
    private String metaKeys;
    private String metaDesc;
    private String createdAt;
    private String updatedAt;
}
