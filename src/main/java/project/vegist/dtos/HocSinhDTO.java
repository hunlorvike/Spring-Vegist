package project.vegist.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class HocSinhDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Age cannot be null")
    private int age;

    @Valid
    @NotNull(message = "Avatar cannot be null")
    private MultipartFile avatar;
}
