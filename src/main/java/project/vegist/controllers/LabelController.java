package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.LabelDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.models.LabelModel;
import project.vegist.repositories.LabelRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.LabelService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class LabelController {
    private final LabelService labelService;
    private final LabelRepository labelRepository;

    @Autowired
    public LabelController(LabelService labelService, LabelRepository labelRepository) {
        this.labelService = labelService;
        this.labelRepository = labelRepository;
    }

    @GetMapping("/labels")
    public ResponseEntity<BaseResponse<List<LabelModel>>> getLabelsWithPagination(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<LabelModel> labels = labelService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(labels));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/labels/{id}")
    public ResponseEntity<BaseResponse<LabelModel>> getLabelById(@PathVariable Long id) {
        try {
            Optional<LabelModel> label = labelService.findById(id);
            return label.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Label not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/labels")
    public ResponseEntity<BaseResponse<LabelModel>> createLabel(@Valid @RequestBody LabelDTO labelDTO) {
        try {
            if (labelRepository.existsByLabelName(labelDTO.getLabelName())) {
                throw new ResourceExistException("label " + labelDTO.getLabelName(), HttpStatus.CONFLICT);
            }

            Optional<LabelModel> createdLabel = labelService.create(labelDTO);
            return createdLabel.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create label"))));
        } catch (ResourceExistException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/labels/batch")
    public ResponseEntity<BaseResponse<List<LabelModel>>> createLabelsBatch(
            @Valid @RequestBody List<LabelDTO> labelDTOs) {
        try {
            List<LabelModel> createdLabels = labelService.createAll(labelDTOs);
            return ResponseEntity.ok(new SuccessResponse<>(createdLabels));
        } catch (ResourceExistException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/labels/{id}")
    public ResponseEntity<BaseResponse<LabelModel>> updateLabel(@PathVariable Long id,
                                                                @RequestBody LabelDTO labelDTO) {
        try {
            Optional<LabelModel> updatedLabel = labelService.update(id, labelDTO);
            return updatedLabel.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Label not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/labels/batch")
    public ResponseEntity<BaseResponse<List<LabelModel>>> updateLabelsBatch(
            @Valid @RequestBody Map<Long, LabelDTO> labelDTOMap) {
        try {
            List<LabelModel> updatedLabels = labelService.updateAll(labelDTOMap);
            return ResponseEntity.ok(new SuccessResponse<>(updatedLabels));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/labels/{id}")
    public ResponseEntity<BaseResponse<String>> deleteLabel(@PathVariable Long id) {
        try {
            boolean deleted = labelService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Label deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Label not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
