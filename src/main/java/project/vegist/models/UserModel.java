package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.vegist.enums.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Long id;
    private String fullName;
    private Gender gender;
    private String email;
    private String phone;
    private String password;
    private String createdAt;
    private String updatedAt;
}
