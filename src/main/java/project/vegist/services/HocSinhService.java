package project.vegist.services;

import org.springframework.stereotype.Service;
import project.vegist.dtos.HocSinhDTO;
import project.vegist.entities.HocSinh;
import project.vegist.models.HocSinhModel;
import project.vegist.services.impls.CrudService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HocSinhService implements CrudService<HocSinh, HocSinhDTO, HocSinhModel> {
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
    public Optional<HocSinhModel> create(HocSinhDTO hocSinhDTO) {
        return Optional.empty();
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
        return null;
    }

    @Override
    public void convertToEntity(HocSinhDTO hocSinhDTO, HocSinh hocSinh) {

    }
}
