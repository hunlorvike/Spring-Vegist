package project.vegist.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hoc_sinh")
public class HocSinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    private String avatarPath;

    @OneToMany(mappedBy = "hocsinh", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AlbumHocSinh> albums;
}
