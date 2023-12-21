package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HocSinhService implements CrudService<HocSinh, HocSinhDTO, HocSinhModel> {
    private final HocSinhRepository hocSinhRepository;
    private final AlbumHocSinhRepository albumHocSinhRepository;
    private final FileUtils fileUtils;

    @Autowired
    public HocSinhService(HocSinhRepository hocSinhRepository, AlbumHocSinhRepository albumHocSinhRepository, FileUtils fileUtils) {
        this.hocSinhRepository = hocSinhRepository;
        this.albumHocSinhRepository = albumHocSinhRepository;
        this.fileUtils = fileUtils;
    }

    @Override
    public List<HocSinhModel> findAll() {
        return hocSinhRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<HocSinhModel> findAll(int page, int size) {
        return null;
    }

    @Override
    public Optional<HocSinhModel> findById(Long id) {
        Optional<HocSinh> hocSinhOptional = hocSinhRepository.findById(id);
        return hocSinhOptional.map(this::convertToModel);

    }

    @Override
    public Optional<HocSinhModel> create(HocSinhDTO hocSinhDTO) throws IOException {
        // Upload avatar và lấy tên file
        String avatarFileName = fileUtils.uploadFile(hocSinhDTO.getAvatar());

        // Lưu thông tin HocSinh
        HocSinh hocSinh = new HocSinh();
        convertToEntity(hocSinhDTO, hocSinh);
        hocSinh.setAvatarPath(avatarFileName);
        hocSinh = hocSinhRepository.save(hocSinh);

        // Xử lý các file trong album nếu có
        List<AlbumHocSinh> albumFiles = new ArrayList<>();
        for (MultipartFile albumFile : hocSinhDTO.getAlbumFiles()) {
            String albumFileName = fileUtils.uploadFile(albumFile);

            AlbumHocSinh albumHocSinh = new AlbumHocSinh();
            albumHocSinh.setAssetsPath(albumFileName);
            albumHocSinh.setHocsinh(hocSinh);
            albumFiles.add(albumHocSinh);
        }

        // Liên kết HocSinh với các file trong album và lưu chúng
        albumHocSinhRepository.saveAll(albumFiles);

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
    public boolean deleteById(Long id) {
        if (hocSinhRepository.existsById(id)) {
            // Retrieve the HocSinh entity
            Optional<HocSinh> optionalHocSinh = hocSinhRepository.findById(id);
            if (optionalHocSinh.isPresent()) {
                HocSinh hocSinh = optionalHocSinh.get();

                // Delete associated AlbumHocSinh records
                List<AlbumHocSinh> albumHocSinhs = albumHocSinhRepository.findByHocsinh_Id(id);
                // FileUtils.deleteFile(FileUtils.joinPaths(uploadDirectory, albumHocSinh.getAssetsPath()));
                albumHocSinhRepository.deleteAll(albumHocSinhs);

                // Delete HocSinh
                hocSinhRepository.deleteById(id);
                return true;
            }
        }
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
        List<AlbumHocSinh> albumHocSinhs = albumHocSinhRepository.findByHocsinh_Id(hocSinh.getId());
        List<String> albumPaths = albumHocSinhs.stream()
                .map(AlbumHocSinh::getAssetsPath)
                .collect(Collectors.toList());

        return new HocSinhModel(hocSinh.getId(), hocSinh.getName(), hocSinh.getAge(), hocSinh.getAvatarPath(), albumPaths);
    }

    @Override
    public void convertToEntity(HocSinhDTO hocSinhDTO, HocSinh hocSinh) {
        hocSinh.setName(hocSinhDTO.getName());
        hocSinh.setAge(hocSinhDTO.getAge());
    }


}
