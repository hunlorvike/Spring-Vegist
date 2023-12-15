package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModel {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer rating;
    private String createdAt;
    private String updatedAt;
}
