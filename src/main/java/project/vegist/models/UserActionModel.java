package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserActionModel {
    private Long id;
    private Long userId;
    private Long actionId;
    private String createdAt;
    private String updatedAt;
}
