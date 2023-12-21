package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.AddressDTO;
import project.vegist.exceptions.NotReadableException;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.exceptions.UnauthorizedException;
import project.vegist.models.AddressModel;
import project.vegist.repositories.UserRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.AddressService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class AddressController {
    private final AddressService addressService;
    private final UserRepository userRepository;

    @Autowired
    public AddressController(AddressService addressService, UserRepository userRepository) {
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    @GetMapping("/user-addresses")
    public ResponseEntity<BaseResponse<List<AddressModel>>> getAllUserAddresses(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<AddressModel> addressModels = addressService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(addressModels, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/user-addresses/{id}")
    public ResponseEntity<BaseResponse<AddressModel>> getUserAddressById(@PathVariable Long id) {
        try {
            Optional<AddressModel> addressModel = addressService.findById(id);
            return addressModel.map(value -> ResponseEntity.ok((BaseResponse<AddressModel>) new SuccessResponse<>(value, null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Address not found", null)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/user-addresses")
    public ResponseEntity<BaseResponse<AddressModel>> createUserAddress(@Valid @RequestBody AddressDTO addressDTO) {
        try {
            Optional<AddressModel> createdAddress = addressService.create(addressDTO);
            return createdAddress.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create user-addresses"))));
        } catch (ResourceExistException | UnauthorizedException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/user-addresses/batch")
    public ResponseEntity<BaseResponse<List<AddressModel>>> batchCreateUserAddresses(
            @RequestBody List<AddressDTO> userAddressDTOs) {
        try {
            List<AddressModel> createdUserAddresses = addressService.createAll(userAddressDTOs);
            return ResponseEntity.ok(new SuccessResponse<>(createdUserAddresses, "Batch create successful"));
        } catch (ResourceExistException | UnauthorizedException | NotReadableException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList("Failed to batch create user-addresses")));
        }
    }

    @PutMapping("/user-addresses/{id}")
    public ResponseEntity<BaseResponse<AddressModel>> updateUserAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO addressDTO) {
        try {
            Optional<AddressModel> updatedUserAddress = addressService.update(id, addressDTO);
            return updatedUserAddress.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("User-address not found"))));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/user-addresses")
    public ResponseEntity<BaseResponse<List<AddressModel>>> batchUpdateUserAddresses(
            @RequestBody Map<Long, AddressDTO> userAddressUpdates) {
        try {
            List<AddressModel> updatedUserAddresses = addressService.updateAll(userAddressUpdates);
            return ResponseEntity.ok(new SuccessResponse<>(updatedUserAddresses, "Batch update successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("user-addresses/{id}")
    public ResponseEntity<BaseResponse<String>> deleteUserAddress(@PathVariable Long id) {
        try {
            boolean deleted = addressService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("User-address deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("User-address not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/user-addresses")
    public ResponseEntity<BaseResponse<String>> batchDeleteUserAddresses(
            @RequestBody List<Long> userAddressIds) {
        try {
            boolean deleted = addressService.deleteAll(userAddressIds);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Batch delete successful"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("One or more user-addresses not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
