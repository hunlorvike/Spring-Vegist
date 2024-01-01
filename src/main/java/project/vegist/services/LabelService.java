package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.LabelDTO;
import project.vegist.entities.Label;
import project.vegist.models.LabelModel;
import project.vegist.repositories.LabelRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.SpecificationsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LabelService implements CrudService<Label, LabelDTO, LabelModel> {
    private final LabelRepository labelRepository;

    @Autowired
    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Transactional(readOnly = true)
    public List<LabelModel> findAll() {
        try {
            return labelRepository.findAll().stream()
                    .map(this::convertToModel)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error while fetching labels", ex);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<LabelModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return labelRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LabelModel> findById(Long id) {
        return labelRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<LabelModel> create(LabelDTO labelDTO) {
        Label newLabel = new Label();
        convertToEntity(labelDTO, newLabel);
        return Optional.ofNullable(convertToModel(labelRepository.save(newLabel)));
    }

    @Override
    @Transactional
    public List<LabelModel> createAll(List<LabelDTO> labelDTOS) {
        List<Label> newLabels = labelDTOS.stream()
                .map(labelDTO -> {
                    Label newLabel = new Label();
                    convertToEntity(labelDTO, newLabel);
                    return newLabel;
                })
                .collect(Collectors.toList());

        return labelRepository.saveAll(newLabels).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<LabelModel> update(Long id, LabelDTO labelDTO) {
        return labelRepository.findById(id)
                .map(existingLabel -> {
                    convertToEntity(labelDTO, existingLabel);
                    return convertToModel(labelRepository.save(existingLabel));
                });
    }

    @Override
    @Transactional
    public List<LabelModel> updateAll(Map<Long, LabelDTO> longLabelDTOMap) {
        return longLabelDTOMap.entrySet().stream()
                .map(entry -> update(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (labelRepository.existsById(id)) {
            labelRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Label> labelsToDelete = labelRepository.findAllById(ids);
        labelRepository.deleteAll(labelsToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelModel> search(String keywords) {
        SpecificationsBuilder<Label> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("labelName", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Label> spec = specificationsBuilder.build();

        return labelRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public LabelModel convertToModel(Label label) {
        return new LabelModel(label.getId(), label.getLabelName());
    }

    @Override
    public void convertToEntity(LabelDTO labelDTO, Label label) {
        label.setLabelName(labelDTO.getLabelName());
    }
}
