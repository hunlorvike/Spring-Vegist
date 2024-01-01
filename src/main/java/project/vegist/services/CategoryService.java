package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.CategoryDTO;
import project.vegist.entities.Category;
import project.vegist.models.CategoryModel;
import project.vegist.repositories.CategoryRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService implements CrudService<Category, CategoryDTO, CategoryModel> {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryModel> findAll() {
        return categoryRepository.findAll().stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findAll(pageable).getContent().stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryModel> findById(Long id) {
        return categoryRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<CategoryModel> create(CategoryDTO categoryDTO) {
        Objects.requireNonNull(categoryDTO, "categoryDTO must not be null");

        Category newCategory = new Category();
        convertToEntity(categoryDTO, newCategory);
        return Optional.ofNullable(convertToModel(categoryRepository.save(newCategory)));
    }


    @Override
    @Transactional
    public List<CategoryModel> createAll(List<CategoryDTO> categoryDTOS) {
        List<Category> newCategories = categoryDTOS.stream()
                .map(categoryDTO -> {
                    Objects.requireNonNull(categoryDTO, "categoryDTO must not be null");

                    Category newCategory = new Category();
                    convertToEntity(categoryDTO, newCategory);
                    return newCategory;
                })
                .collect(Collectors.toList());

        List<Category> savedCategories = categoryRepository.saveAll(newCategories);
        return savedCategories.stream().map(this::convertToModel).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public Optional<CategoryModel> update(Long id, CategoryDTO categoryDTO) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    convertToEntity(categoryDTO, existingCategory);
                    return convertToModel(categoryRepository.save(existingCategory));
                });
    }

    @Override
    @Transactional
    public List<CategoryModel> updateAll(Map<Long, CategoryDTO> longCategoryDTOMap) {
        return longCategoryDTOMap.entrySet().stream()
                .map(entry -> {
                    Long id = entry.getKey();
                    CategoryDTO categoryDTO = entry.getValue();

                    if (categoryDTO != null) {
                        return categoryRepository.findById(id)
                                .map(existingCategory -> {
                                    convertToEntity(categoryDTO, existingCategory);
                                    return existingCategory;
                                })
                                .orElse(null);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(categoryRepository::save)
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return categoryRepository.existsById(id) && performDelete(id);
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Category> categoriesToDelete = categoryRepository.findAllById(ids);
        if (!categoriesToDelete.isEmpty()) {
            categoryRepository.deleteAll(categoriesToDelete);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryModel> search(String keywords) {
        SpecificationsBuilder<Category> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .like("name", keywords) // Add more fields if needed
                    .or(builder -> {
                        // Add additional search conditions if needed
                        builder.like("seoTitle", keywords);
                        builder.like("metaKeys", keywords);
                        builder.like("metaDesc", keywords);
                    });
        }

        return categoryRepository.findAll(specificationsBuilder.build()).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryModel convertToModel(Category category) {
        return new CategoryModel(category.getId(), category.getName(), category.getStatus(), getParentId(category.getParent()), category.getSeoTitle(), category.getMetaKeys(), category.getMetaDesc(), DateTimeUtils.formatLocalDateTime(category.getCreatedAt()), DateTimeUtils.formatLocalDateTime(category.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(CategoryDTO categoryDTO, Category category) {
        Objects.requireNonNull(categoryDTO, "categoryDTO must not be null");
        Objects.requireNonNull(category, "category must not be null");

        Long parentId = categoryDTO.getParentId();
        if (parentId != null) {
            Optional<Category> parentCategory = categoryRepository.findById(parentId);
            parentCategory.ifPresent(category::setParent);
        }

        category.setName(categoryDTO.getName());
        category.setStatus(categoryDTO.getStatus());
        category.setSeoTitle(categoryDTO.getSeoTitle());
        category.setMetaKeys(categoryDTO.getMetaKeys());
        category.setMetaDesc(categoryDTO.getMetaDesc());
    }


    private boolean performDelete(Long id) {
        categoryRepository.deleteById(id);
        return true;
    }

    private Long getParentId(Category parent) {
        return (parent != null) ? parent.getId() : null;
    }

}
