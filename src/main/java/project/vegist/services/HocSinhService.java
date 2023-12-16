package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.dtos.HocSinhDTO;
import project.vegist.entities.AlbumHocSinh;
import project.vegist.entities.HocSinh;
import project.vegist.models.HocSinhModel;
import project.vegist.repositories.AlbumHocSinhRepository;
import project.vegist.repositories.HocSinhRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class HocSinhService implements CrudService<HocSinh, HocSinhDTO, HocSinhModel> {
    private final HocSinhRepository hocSinhRepository;
    private final AlbumHocSinhRepository albumHocSinhRepository;

    @Value("${upload-path}")
    private String uploadDirectory;

    @Autowired
    public HocSinhService(HocSinhRepository hocSinhRepository, AlbumHocSinhRepository albumHocSinhRepository) {
        this.hocSinhRepository = hocSinhRepository;
        this.albumHocSinhRepository = albumHocSinhRepository;
    }

    @Override
    public List<HocSinhModel> findAll() {
        return null;
    }

    @Override
    public List<HocSinhModel> findAll(int page, int size) {
        return null;
    }

    @Override
    public Optional<HocSinhModel> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<HocSinhModel> create(HocSinhDTO hocSinhDTO) throws IOException {
        // Upload avatar và lấy tên file
        String avatarFileName = uploadFile(hocSinhDTO.getAvatar());

        // Lưu thông tin HocSinh
        HocSinh hocSinh = new HocSinh();
        convertToEntity(hocSinhDTO, hocSinh);
        hocSinh.setAvatarPath(avatarFileName);
        hocSinh = hocSinhRepository.save(hocSinh);

        // Xử lý các file trong album nếu có
        List<AlbumHocSinh> albumFiles = new ArrayList<>();
        for (MultipartFile albumFile : hocSinhDTO.getAlbumFiles()) {
            String albumFileName = uploadFile(albumFile);

            AlbumHocSinh albumHocSinh = new AlbumHocSinh();
            albumHocSinh.setAssetsPath(albumFileName);
            albumHocSinh.setHocsinh(hocSinh);
            albumFiles.add(albumHocSinh);
        }

        // Liên kết HocSinh với các file trong album và lưu chúng
        for (AlbumHocSinh albumHocSinh : albumFiles) {
            albumHocSinhRepository.save(albumHocSinh);
        }

        return Optional.ofNullable(convertToModel(hocSinh));
    }


    @Override
    public List<HocSinhModel> createAll(List<HocSinhDTO> hocSinhDTOS) {
        return null;
    }

    @Override
    public Optional<HocSinhModel> update(Long id, HocSinhDTO hocSinhDTO) {
        return Optional.empty();
    }

    @Override
    public List<HocSinhModel> updateAll(Map<Long, HocSinhDTO> longHocSinhDTOMap) {
        return null;
    }

    @Override
    public boolean deleleById(Long id) {
        return false;
    }

    @Override
    public boolean deleteAll(List<Long> ids) {
        return false;
    }

    @Override
    public List<HocSinhModel> search(String keywords) {
        return null;
    }

    @Override
    public HocSinhModel convertToModel(HocSinh hocSinh) {
        return new HocSinhModel(hocSinh.getId(), hocSinh.getName(), hocSinh.getAge(), hocSinh.getAvatarPath());
    }

    @Override
    public void convertToEntity(HocSinhDTO hocSinhDTO, HocSinh hocSinh) {
        hocSinh.setName(hocSinhDTO.getName());
        hocSinh.setAge(hocSinhDTO.getAge());
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = FileUtils.getFileExtension(originalFileName);

        String subFolder;
        if (isImageFile(fileExtension)) {
            subFolder = "images";
        } else if (isVideoFile(fileExtension)) {
            subFolder = "videos";
        } else {
            subFolder = "other";
        }

        String folderPath = FileUtils.joinPaths(uploadDirectory, subFolder);
        Files.createDirectories(Paths.get(folderPath));

        String uniqueFileName = FileUtils.generateUniqueFileName(originalFileName);
        String filePath = FileUtils.joinPaths(folderPath, uniqueFileName);

        FileUtils.saveFile(file, filePath);

        return uniqueFileName;
    }


    private boolean isImageFile(String fileExtension) {
        return Arrays.asList("jpg", "png", "gif").contains(fileExtension.toLowerCase());
    }

    private boolean isVideoFile(String fileExtension) {
        return Arrays.asList("mp4", "avi", "mkv").contains(fileExtension.toLowerCase());
    }

}
