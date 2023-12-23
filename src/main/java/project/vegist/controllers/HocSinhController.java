package project.vegist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.dtos.HocSinhDTO;
import project.vegist.models.HocSinhModel;
import project.vegist.services.HocSinhService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class HocSinhController {

    private final HocSinhService hocSinhService;

    @Autowired
    public HocSinhController(HocSinhService hocSinhService) {
        this.hocSinhService = hocSinhService;
    }

    @GetMapping("/hoc-sinh")
    public ResponseEntity<List<HocSinhModel>> findAllHocSinhs() {
        List<HocSinhModel> hocSinhs = hocSinhService.findAll();
        return ResponseEntity.ok(hocSinhs);
    }

    @GetMapping("/hoc-sinh/{id}")
    public ResponseEntity<HocSinhModel> findHocSinhById(@PathVariable Long id) {
        Optional<HocSinhModel> hocSinhModel = hocSinhService.findById(id);
        return hocSinhModel
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/hoc-sinh")
    public ResponseEntity<HocSinhModel> createHocSinh(
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "albumFiles", required = false) MultipartFile[] albumFiles
    ) {
        try {
            HocSinhDTO hocSinhDTO = new HocSinhDTO();
            hocSinhDTO.setName(name);
            hocSinhDTO.setAge(age);
            hocSinhDTO.setAvatar(avatar);
            hocSinhDTO.setAlbumFiles(albumFiles);

            Optional<HocSinhModel> createdHocSinh = hocSinhService.create(hocSinhDTO);

            return createdHocSinh
                    .map(hocSinhModel -> new ResponseEntity<>(hocSinhModel, HttpStatus.CREATED))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/hoc-sinh/{id}")
    public ResponseEntity<Void> deleteHocSinh(@PathVariable Long id) {
        if (hocSinhService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
