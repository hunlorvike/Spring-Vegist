package project.vegist.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TagDTO {
    @NotBlank(message = "Tag name can't blank")
    private String tagName;
    @NotNull(message = "Status can't be null")
    private boolean status;
}
