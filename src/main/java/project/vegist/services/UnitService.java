package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.UnitDTO;
import project.vegist.entities.Unit;
import project.vegist.models.UnitModel;
import project.vegist.repositories.UnitRepository;
import project.vegist.services.impls.CrudService;

import java.util.ArrayList;
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
        List<UnitModel> updatedUnitModels = new ArrayList<>();
        for (Map.Entry<Long, UnitDTO> entry : longUnitDTOMap.entrySet()) {
            Long unitId = entry.getKey();
            UnitDTO unitDTO = entry.getValue();

            Optional<Unit> optionalUnit = unitRepository.findById(unitId);

            if (optionalUnit.isPresent()) {
                Unit existingUnit = optionalUnit.get();
                convertToEntity(unitDTO, existingUnit);
                Unit updatedUnit = unitRepository.save(existingUnit);
                updatedUnitModels.add(convertToModel(updatedUnit));
            }
        }
        return updatedUnitModels;
    }

    @Override
    @Transactional
    public boolean deleleById(Long id) {
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
        // Implement search logic based on your requirements
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public UnitModel convertToModel(Unit unit) {
        return new UnitModel(unit.getId(), unit.getUnitValue(), unit.getUnitName());
    }

    @Override
    public void convertToEntity(UnitDTO unitDTO, Unit unit) {
        unit.setUnitValue(unitDTO.getUnitValue());
        unit.setUnitName(unitDTO.getUnitName());
    }
}
