package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.ActionDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.exceptions.UnauthorizedException;
import project.vegist.models.ActionModel;
import project.vegist.repositories.ActionRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.ActionService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class ActionController {
    private final ActionService actionService;
    private final ActionRepository actionRepository;

    @Autowired
    public ActionController(ActionService actionService, ActionRepository actionRepository) {
        this.actionService = actionService;
        this.actionRepository = actionRepository;
    }

    @GetMapping("/actions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<ActionModel>>> getAllActions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            List<ActionModel> actions = actionService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(actions));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/actions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ActionModel>> getActionById(@PathVariable Long id) {
        try {
            Optional<ActionModel> action = actionService.findById(id);
            return action.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Action not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/actions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ActionModel>> createAction(@Valid @RequestBody ActionDTO actionDTO) {
        try {
            if (actionRepository.existsByActionName(actionDTO.getActionName())) {
                throw new ResourceExistException("action " + actionDTO.getActionName(), HttpStatus.CONFLICT);
            }

            Optional<ActionModel> createdAction = actionService.create(actionDTO);
            return createdAction.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create action"))));
        } catch (ResourceExistException | UnauthorizedException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/actions/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<ActionModel>>> createBulkActions(
            @RequestBody List<ActionDTO> actionDTOList) {
        try {
            List<ActionModel> createdActions = actionService.createAll(actionDTOList);
            return ResponseEntity.ok(new SuccessResponse<>(createdActions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/actions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ActionModel>> updateAction(@PathVariable Long id,
                                                                  @RequestBody ActionDTO actionDTO) {
        try {
            Optional<ActionModel> updatedAction = actionService.update(id, actionDTO);
            return updatedAction.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Action not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/actions/bulk")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<ActionModel>>> updateBulkActions(
            @RequestBody Map<Long, ActionDTO> actionDTOMap) {
        try {
            List<ActionModel> updatedActions = actionService.updateAll(actionDTOMap);
            return ResponseEntity.ok(new SuccessResponse<>(updatedActions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/actions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteAction(@PathVariable Long id) {
        try {
            boolean deleted = actionService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Action deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Action not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/actions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteAllActions(
            @RequestBody List<Long> actionIds) {
        try {
            boolean deleted = actionService.deleteAll(actionIds);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Actions deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("One or more actions not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
