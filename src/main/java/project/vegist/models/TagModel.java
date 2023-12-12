package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TagModel {
    private Long id;
    private String tagName;
    private boolean status;
    private String createdAt;
    private String updatedAt;

}
