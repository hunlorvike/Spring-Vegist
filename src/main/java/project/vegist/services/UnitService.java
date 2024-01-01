package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.UnitDTO;
import project.vegist.entities.Unit;
import project.vegist.models.UnitModel;
import project.vegist.repositories.UnitRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.SpecificationsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UnitService implements CrudService<Unit, UnitDTO, UnitModel> {
    private final UnitRepository unitRepository;

    @Autowired
    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitModel> findAll() {
        return unitRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return unitRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnitModel> findById(Long id) {
        return unitRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<UnitModel> create(UnitDTO unitDTO) {
        Unit newUnit = new Unit();
        convertToEntity(unitDTO, newUnit);
        Unit savedUnit = unitRepository.save(newUnit);
        return Optional.ofNullable(convertToModel(savedUnit));
    }

    @Override
    @Transactional
    public List<UnitModel> createAll(List<UnitDTO> unitDTOS) {
        List<Unit> unitsToSave = unitDTOS.stream()
                .map(unitDTO -> {
                    Unit newUnit = new Unit();
                    convertToEntity(unitDTO, newUnit);
                    return newUnit;
                })
                .collect(Collectors.toList());

        List<Unit> savedUnits = unitRepository.saveAll(unitsToSave);
        return savedUnits.stream().map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UnitModel> update(Long id, UnitDTO unitDTO) {
        return unitRepository.findById(id)
                .map(existingUnit -> {
                    convertToEntity(unitDTO, existingUnit);
                    Unit updatedUnit = unitRepository.save(existingUnit);
                    return convertToModel(updatedUnit);
                });
    }

    @Override
    @Transactional
    public List<UnitModel> updateAll(Map<Long, UnitDTO> longUnitDTOMap) {
        List<UnitModel> updatedUnitModels = longUnitDTOMap.entrySet().stream()
                .map(entry -> update(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream) // Remove empty Optionals
                .collect(Collectors.toList());

        return updatedUnitModels;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (unitRepository.existsById(id)) {
            unitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Unit> unitsToDelete = unitRepository.findAllById(ids);
        unitRepository.deleteAll(unitsToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitModel> search(String keywords) {
        SpecificationsBuilder<Unit> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("unitValue", keywords);
                        builder.like("unitName", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Unit> spec = specificationsBuilder.build();

        return unitRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnitModel convertToModel(Unit unit) {
        return new UnitModel(unit.getId(), unit.getUnitValue(), unit.getUnitName());
    }

    @Override
    public void convertToEntity(UnitDTO unitDTO, Unit unit) {
        if (unitDTO.getUnitValue() != null) {
            unit.setUnitValue(unitDTO.getUnitValue());
        }
        if (unitDTO.getUnitName() != null) {
            unit.setUnitName(unitDTO.getUnitName());
        }
    }
}
