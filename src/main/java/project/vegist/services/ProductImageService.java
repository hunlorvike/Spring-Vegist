package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.ProductImageDTO;
import project.vegist.entities.ProductImage;
import project.vegist.models.ProductImageModel;
import project.vegist.repositories.ProductImageRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductImageService implements CrudService<ProductImage, ProductImageDTO, ProductImageModel> {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductImageService(ProductImageRepository productImageRepository, ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductImageModel> findAll() {
        return productImageRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductImageModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productImageRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductImageModel> findById(Long id) {
        return productImageRepository.findById(id).map(this::convertToModel);
    }

    public List<ProductImageModel> findByProductId(Long id) {
        List<ProductImage> productImages = productImageRepository.findByProduct_Id(id);
        return productImages.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    public Optional<ProductImageModel> create(ProductImageDTO productImageDTO) throws IOException {
        ProductImage newProductImage = new ProductImage();
        convertToEntity(productImageDTO, newProductImage);
        return Optional.ofNullable(convertToModel(productImageRepository.save(newProductImage)));
    }

    @Override
    public List<ProductImageModel> createAll(List<ProductImageDTO> productImageDTOS) throws IOException {
        List<ProductImage> newProductImages = productImageDTOS.stream()
                .map(dto -> {
                    ProductImage productImage = new ProductImage();
                    convertToEntity(dto, productImage);
                    return productImage;
                })
                .collect(Collectors.toList());

        List<ProductImage> savedProductImages = productImageRepository.saveAll(newProductImages);
        return savedProductImages.stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public Optional<ProductImageModel> update(Long id, ProductImageDTO productImageDTO) {
        Optional<ProductImage> existingProductImage = productImageRepository.findById(id);
        return existingProductImage.map(productImage -> {
            convertToEntity(productImageDTO, productImage);
            ProductImage updatedProductImage = productImageRepository.save(productImage);
            return convertToModel(updatedProductImage);
        });
    }

    @Override
    public List<ProductImageModel> updateAll(Map<Long, ProductImageDTO> longProductImageDTOMap) {
        return null;
    }

    @Override
    public boolean deleteById(Long id) {
        if (productImageRepository.existsById(id)) {
            productImageRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteAll(List<Long> ids) {
        List<ProductImage> productImagesToDelete = productImageRepository.findAllById(ids);
        if (!productImagesToDelete.isEmpty()) {
            productImageRepository.deleteAll(productImagesToDelete);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public List<ProductImageModel> search(String keywords) {
        SpecificationsBuilder<ProductImage> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("product.name", keywords);
                        builder.like("imagePath", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<ProductImage> spec = specificationsBuilder.build();

        return productImageRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageModel convertToModel(ProductImage productImage) {
        return new ProductImageModel(productImage.getId(), productImage.getProduct().getId(), productImage.getImagePath());
    }

    @Override
    public void convertToEntity(ProductImageDTO productImageDTO, ProductImage productImage) {
        productImage.setProduct(productRepository.findById(productImageDTO.getProductId()).get());
        productImage.setImagePath(productImageDTO.getImagePath());
    }
}
