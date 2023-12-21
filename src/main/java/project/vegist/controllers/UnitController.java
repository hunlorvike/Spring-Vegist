package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.UnitDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.UnitModel;
import project.vegist.repositories.UnitRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.UnitService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class UnitController {
    private final UnitService unitService;
    private final UnitRepository unitRepository;

    @Autowired
    public UnitController(UnitService unitService, UnitRepository unitRepository) {
        this.unitService = unitService;
        this.unitRepository = unitRepository;
    }

    @GetMapping("/units")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<UnitModel>>> getUnitsWithPagination(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<UnitModel> units = unitService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(units));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/units/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<UnitModel>> getUnitById(@PathVariable Long id) {
        try {
            Optional<UnitModel> unit = unitService.findById(id);
            return unit.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Unit not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/units")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<UnitModel>> createUnit(@Valid @RequestBody UnitDTO unitDTO) {
        try {
            if (unitRepository.existsByUnitValue(unitDTO.getUnitValue())) {
                throw new ResourceExistException("unit with value " + unitDTO.getUnitValue(), HttpStatus.CONFLICT);
            }

            Optional<UnitModel> createdUnit = unitService.create(unitDTO);
            return createdUnit.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create unit"))));
        } catch (ResourceExistException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/units/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<UnitModel>> updateUnit(@PathVariable Long id,
                                                              @RequestBody UnitDTO unitDTO) {
        try {
            Optional<UnitModel> updatedUnit = unitService.update(id, unitDTO);
            return updatedUnit.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Unit not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/units/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteUnit(@PathVariable Long id) {
        try {
            boolean deleted = unitService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Unit deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Unit not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
