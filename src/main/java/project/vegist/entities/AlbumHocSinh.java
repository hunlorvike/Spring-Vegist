package project.vegist.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "album_hoc_sinh")
@Entity
public class AlbumHocSinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hocsinh_id", nullable = false, foreignKey = @ForeignKey(name = "fk_album_hoc_sinh_hoc_sinh"))
    private HocSinh hocsinh;

    @Column(name = "assets_path", nullable = false)
    private String assetsPath;
}
