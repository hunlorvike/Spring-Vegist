package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.InventoryDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.InventoryModel;
import project.vegist.repositories.InventoryRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.InventoryService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/private")
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryController(InventoryService inventoryService, InventoryRepository inventoryRepository) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/inventories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<InventoryModel>>> getAllInventories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            List<InventoryModel> inventories = inventoryService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(inventories));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/inventories/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<InventoryModel>> getInventoryById(@PathVariable Long id) {
        try {
            Optional<InventoryModel> inventory = inventoryService.findById(id);
            return inventory.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Inventory not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("failed", e.getMessage(), null));
        }
    }

    @PostMapping("/inventories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<InventoryModel>> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        try {
            Optional<InventoryModel> createdInventory = inventoryService.create(inventoryDTO);
            return createdInventory.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create inventory"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("/inventories/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<InventoryModel>>> createBatchInventories(@RequestBody List<InventoryDTO> inventoryDTOList) {
        try {
            List<InventoryModel> createdInventories = inventoryService.createAll(inventoryDTOList);
            return ResponseEntity.ok(new SuccessResponse<>(createdInventories));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList("Failed to create batch inventories")));
        }
    }

    @PutMapping("/inventories/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<InventoryModel>> updateInventory(@PathVariable Long id, @RequestBody InventoryDTO inventoryDTO) {
        try {
            Optional<InventoryModel> updatedInventory = inventoryService.update(id, inventoryDTO);
            return updatedInventory.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Inventory not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/inventories/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<InventoryModel>>> updateBatchInventories(@RequestBody Map<Long, InventoryDTO> inventoryDTOMap) {
        try {
            List<InventoryModel> updatedInventories = inventoryService.updateAll(inventoryDTOMap);
            return ResponseEntity.ok(new SuccessResponse<>(updatedInventories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/inventories/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteInventory(@PathVariable Long id) {
        try {
            boolean deleted = inventoryService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Inventory deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Inventory not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/inventories/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteBatchInventories(@RequestParam List<Long> ids) {
        try {
            boolean deleted = inventoryService.deleteAll(ids);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Inventories deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("One or more inventories not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
