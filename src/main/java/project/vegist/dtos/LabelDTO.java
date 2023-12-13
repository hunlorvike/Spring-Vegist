package project.vegist.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelDTO {
    @NotBlank(message = "Label name cannot be blank")
    private String labelName;
}
