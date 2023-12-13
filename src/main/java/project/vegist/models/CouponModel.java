package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponModel {
    private Long id;
    private String value;
    private int percent;
    private String startDate;
    private String endDate;
}

