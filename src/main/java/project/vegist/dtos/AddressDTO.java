package project.vegist.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import project.vegist.enums.AddressType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Detail cannot be blank")
    private String detail;

    @NotBlank(message = "Ward cannot be blank")
    private String ward;

    @NotBlank(message = "District cannot be blank")
    private String district;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotBlank(message = "Zip code cannot be blank")
    @Size(min = 5, max = 10, message = "Zip code must be between 5 and 10 characters")
    private String zipCode;

    private String iframeAddress;

    @NotNull(message = "Address type cannot be null")
    private AddressType addressType;
}
