package project.vegist.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDTO {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Unit ID cannot be null")
    private Long unitId;
}



