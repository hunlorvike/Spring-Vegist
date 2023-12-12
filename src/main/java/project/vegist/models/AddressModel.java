package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import project.vegist.enums.AddressType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddressModel {
    private Long id;
    private Long userId;
    private String detail;
    private String ward;
    private String district;
    private String city;
    private String country;
    private String zipCode;
    private String iframeAddress;
    private AddressType addressType;
    private String createdAt;
    private String updatedAt;
}
