package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.TagDTO;
import project.vegist.entities.Tag;
import project.vegist.models.TagModel;
import project.vegist.repositories.TagRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService implements CrudService<Tag, TagDTO, TagModel> {
    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagModel> findAll() {
        return tagRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tagRepository.findAll(pageable).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TagModel> findById(Long id) {
        return tagRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<TagModel> create(TagDTO tagDTO) {
        Tag newTag = new Tag();
        convertToEntity(tagDTO, newTag);
        Tag savedTag = tagRepository.save(newTag);
        return Optional.ofNullable(convertToModel(savedTag));
    }

    @Override
    @Transactional
    public List<TagModel> createAll(List<TagDTO> tagDTOS) {
        List<Tag> tagsToSave = tagDTOS.stream()
                .map(tagDTO -> {
                    Tag newTag = new Tag();
                    convertToEntity(tagDTO, newTag);
                    return newTag;
                })
                .collect(Collectors.toList());

        List<Tag> savedTags = tagRepository.saveAll(tagsToSave);
        return savedTags.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<TagModel> update(Long id, TagDTO tagDTO) {
        return tagRepository.findById(id)
                .map(existingTag -> {
                    convertToEntity(tagDTO, existingTag);
                    Tag updatedTag = tagRepository.save(existingTag);
                    return convertToModel(updatedTag);
                });
    }

    @Override
    @Transactional
    public List<TagModel> updateAll(Map<Long, TagDTO> longTagDTOMap) {
        return longTagDTOMap.entrySet().stream()
                .map(entry -> {
                    Long tagId = entry.getKey();
                    TagDTO tagDTO = entry.getValue();

                    return tagRepository.findById(tagId)
                            .map(existingTag -> {
                                convertToEntity(tagDTO, existingTag);
                                Tag updatedTag = tagRepository.save(existingTag);
                                return convertToModel(updatedTag);
                            })
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleleById(Long id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Tag> tagsToDelete = tagRepository.findAllById(ids);
        if (!tagsToDelete.isEmpty()) {
            tagRepository.deleteAll(tagsToDelete);
            return true;
        }
        return false;
    }

    @Override
    public List<TagModel> search(String keywords) {
        // Implement your search logic
        return null;
    }

    @Override
    public TagModel convertToModel(Tag tag) {
        return new TagModel(tag.getId(), tag.getTagName(), tag.isStatus(),
                DateTimeUtils.formatLocalDateTime(tag.getCreatedAt()), DateTimeUtils.formatLocalDateTime(tag.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(TagDTO tagDTO, Tag tag) {
        tag.setTagName(tagDTO.getTagName());
        tag.setStatus(tagDTO.isStatus());
    }
}
