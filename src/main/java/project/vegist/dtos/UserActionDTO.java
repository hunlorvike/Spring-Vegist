package project.vegist.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Action ID cannot be null")
    private Long actionId;
}
