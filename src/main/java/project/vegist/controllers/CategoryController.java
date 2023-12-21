package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.CategoryDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.CategoryModel;
import project.vegist.repositories.CategoryRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.CategoryService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/public")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryRepository categoryRepository) {
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<List<CategoryModel>>> getAllCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<CategoryModel> categoryModels = categoryService.findAll(page, size);
            return ResponseEntity.ok(new SuccessResponse<>(categoryModels, "Thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<BaseResponse<CategoryModel>> getCategoryById(@PathVariable Long id) {
        try {
            Optional<CategoryModel> categoryModel = categoryService.findById(id);
            return categoryModel.map(value -> ResponseEntity.ok((BaseResponse<CategoryModel>) new SuccessResponse<>(value, null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Category not found", null)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<BaseResponse<CategoryModel>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            if (categoryRepository.existsByName(categoryDTO.getName())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse<>(Collections.singletonList("Category with this name already exists")));
            }

            if (categoryDTO.getParentId() != null && !categoryRepository.existsById(categoryDTO.getParentId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse<>(Collections.singletonList("Parent category with this id does not exist")));
            }

            Optional<CategoryModel> createdCategory = categoryService.create(categoryDTO);
            return createdCategory.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create category"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("/categories/batch")
    public ResponseEntity<BaseResponse<List<CategoryModel>>> createAllCategories(@Valid @RequestBody List<CategoryDTO> categoryDTOs) {
        try {
            List<String> errors = categoryDTOs.stream()
                    .flatMap(categoryDTO -> Stream.of(
                            categoryRepository.existsByName(categoryDTO.getName()) ? "Category with this name already exists" : null,
                            (categoryDTO.getParentId() != null && !categoryRepository.existsById(categoryDTO.getParentId())) ?
                                    "Parent category with this id does not exist" : null
                    ))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse<>(errors));
            }

            List<CategoryModel> createdCategories = categoryService.createAll(categoryDTOs);
            return ResponseEntity.ok(new SuccessResponse<>(createdCategories, "Categories created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }


    @PutMapping("/categories/{id}")
    public ResponseEntity<BaseResponse<CategoryModel>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            Optional<CategoryModel> updatedCategory = categoryService.update(id, categoryDTO);
            return updatedCategory.map(value -> ResponseEntity.ok(new BaseResponse<>("success", null, value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Category not found"))));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/categories/batch")
    public ResponseEntity<BaseResponse<List<CategoryModel>>> updateCategories(@Valid @RequestBody Map<Long, CategoryDTO> categoryDTOMap) {
        try {
            List<CategoryModel> updatedCategories = categoryService.updateAll(categoryDTOMap);
            return ResponseEntity.ok(new SuccessResponse<>(updatedCategories, "Categories updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<BaseResponse<String>> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteById(id);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Category deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Category not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/categories")
    public ResponseEntity<BaseResponse<String>> deleteCategories(@RequestBody List<Long> categoryIds) {
        try {
            boolean deleted = categoryService.deleteAll(categoryIds);
            return deleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Categories deleted successfully"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Categories not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
