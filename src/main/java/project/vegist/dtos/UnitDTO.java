package project.vegist.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitDTO {
    @NotNull(message = "Unit value must be specified")
    private Integer unitValue;

    @NotBlank(message = "Unit name cannot be blank")
    private String unitName;
}
