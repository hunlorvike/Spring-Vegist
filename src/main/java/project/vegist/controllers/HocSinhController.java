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
        return new ResponseEntity<>(hocSinhs, HttpStatus.OK);
    }

    @GetMapping("/hoc-sinh/{id}")
    public ResponseEntity<HocSinhModel> findHocSinhById(@PathVariable Long id) {
        return hocSinhService.findById(id)
                .map(hocSinhModel -> new ResponseEntity<>(hocSinhModel, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/hoc-sinh")
    public ResponseEntity<HocSinhModel> createHocSinh(
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestPart("avatar") MultipartFile avatar,
            @RequestPart(value = "albumFiles", required = false) MultipartFile[] albumFiles
    ) {
        try {
            HocSinhDTO hocSinhDTO = new HocSinhDTO();
            hocSinhDTO.setName(name);
            hocSinhDTO.setAge(age);
            hocSinhDTO.setAvatar(avatar);
            hocSinhDTO.setAlbumFiles(albumFiles);

            // Gọi service để tạo mới HocSinhModel
            return hocSinhService.create(hocSinhDTO)
                    .map(hocSinhModel -> new ResponseEntity<>(hocSinhModel, HttpStatus.CREATED))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/hoc-sinh/{id}")
    public ResponseEntity<Void> deleteHocSinh(@PathVariable Long id) {
        if (hocSinhService.deleteById(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
