package project.vegist.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HocSinhModel {
    private Long id;
    private String name;
    private int age;
    private String avatarPath;
    private List<String> albumPaths;
}
