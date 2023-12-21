package project.vegist.services.impls;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CrudService<Entity, DTO, Model> {
    @Transactional(readOnly = true)
    List<Model> findAll();

    @Transactional(readOnly = true)
    List<Model> findAll(int page, int size);

    @Transactional(readOnly = true)
    Optional<Model> findById(Long id);

    @Transactional
    Optional<Model> create(DTO dto) throws IOException;

    @Transactional
    List<Model> createAll(List<DTO> dtos) throws IOException;

    @Transactional
    Optional<Model> update(Long id, DTO dto);

    @Transactional
    List<Model> updateAll(Map<Long, DTO> dtoMap);

    @Transactional
    boolean deleteById(Long id);

    @Transactional
    boolean deleteAll(List<Long> ids);

    @Transactional
    List<Model> search(String keywords);

    Model convertToModel(Entity entity);

    void convertToEntity(DTO dto, Entity entity);

    // Phương thức mặc định không cần triển khai tại các class implement interface
    default void testPhuongThucDefault() {
    }
}
