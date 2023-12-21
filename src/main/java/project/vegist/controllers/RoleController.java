package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.RoleDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.models.RoleModel;
import project.vegist.repositories.RoleRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.RoleService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class RoleController {
    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @Autowired
    public RoleController(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/roles")
    public ResponseEntity<BaseResponse<List<RoleModel>>> getRolesWithPagination(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<RoleModel> roles = roleService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<BaseResponse<RoleModel>> getRoleById(@PathVariable Long id) {
        try {
            Optional<RoleModel> role = roleService.findById(id);
            return role.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Role not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<BaseResponse<RoleModel>> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        try {
            if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
                throw new ResourceExistException("role " + roleDTO.getRoleName(), HttpStatus.CONFLICT);
            }

            Optional<RoleModel> createdRole = roleService.create(roleDTO);
            return createdRole.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create role"))));
        } catch (ResourceExistException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/roles/batch")
    public ResponseEntity<BaseResponse<List<RoleModel>>> createRolesBatch(
            @Valid @RequestBody List<RoleDTO> roleDTOs) {
        try {
            List<RoleModel> createdRoles = roleService.createAll(roleDTOs);
            return ResponseEntity.ok(new SuccessResponse<>(createdRoles));
        } catch (ResourceExistException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<BaseResponse<RoleModel>> updateRole(@PathVariable Long id,
                                                              @RequestBody RoleDTO roleDTO) {
        try {
            Optional<RoleModel> updatedRole = roleService.update(id, roleDTO);
            return updatedRole.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Role not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/roles/batch")
    public ResponseEntity<BaseResponse<List<RoleModel>>> updateRolesBatch(
            @Valid @RequestBody Map<Long, RoleDTO> roleDTOMap) {
        try {
            List<RoleModel> updatedRoles = roleService.updateAll(roleDTOMap);
            return ResponseEntity.ok(new SuccessResponse<>(updatedRoles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<BaseResponse<String>> deleteRole(@PathVariable Long id) {
        try {
            boolean deleted = roleService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Role deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Role not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
