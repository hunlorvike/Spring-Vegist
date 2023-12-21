package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.UserActionDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.exceptions.UnauthorizedException;
import project.vegist.models.UserActionModel;
import project.vegist.repositories.ActionRepository;
import project.vegist.repositories.UserActionRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.UserActionService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
public class UserActionController {
    private final UserActionService userActionService;
    private final UserActionRepository userActionRepository;
    private final UserRepository userRepository;
    private final ActionRepository actionRepository;

    @Autowired
    public UserActionController(UserActionService userActionService, UserActionRepository userActionRepository, UserRepository userRepository, ActionRepository actionRepository) {
        this.userActionService = userActionService;
        this.userActionRepository = userActionRepository;
        this.userRepository = userRepository;
        this.actionRepository = actionRepository;
    }

    @GetMapping("private/user-actions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<UserActionModel>>> getAllUserActions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<UserActionModel> userActions = userActionService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(userActions, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("public/user-actions/{id}")
    public ResponseEntity<BaseResponse<UserActionModel>> getUserActionById(@PathVariable Long id) {
        try {
            Optional<UserActionModel> userAction = userActionService.findById(id);
            return userAction.map(value -> ResponseEntity.ok((BaseResponse<UserActionModel>) new SuccessResponse<>(value, null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Action not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("public/user-actions")
    public ResponseEntity<BaseResponse<UserActionModel>> createUserAction(@Valid @RequestBody UserActionDTO userActionDTO) {
        try {
            Optional<UserActionModel> createdUserAction = userActionService.create(userActionDTO);
            return createdUserAction.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create user-actions"))));
        } catch (ResourceExistException | UnauthorizedException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/private/user-actions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<UserActionModel>> updateUserAction(@PathVariable Long id, @Valid @RequestBody UserActionDTO userActionDTO) {
        try {
            Optional<UserActionModel> updatedUserAction = userActionService.update(id, userActionDTO);
            return updatedUserAction.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("User-action not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/private/user-actions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteUserAction(@PathVariable Long id) {
        try {
            boolean deleted = userActionService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("User-action deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("User-action not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

}
