package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.vegist.dtos.ProductDTO;
import project.vegist.entities.Product;
import project.vegist.models.ProductModel;
import project.vegist.repositories.CategoryRepository;
import project.vegist.repositories.LabelRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService implements CrudService<Product, ProductDTO, ProductModel> {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public List<ProductModel> findAll() {
        return productRepository.findAll().stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public List<ProductModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable).stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public Optional<ProductModel> findById(Long id) {
        return productRepository.findById(id).map(this::convertToModel);
    }

    @Override
    public Optional<ProductModel> create(ProductDTO productDTO) {
        return Optional.empty();
    }

    @Override
    public List<ProductModel> createAll(List<ProductDTO> productDTOS) {
        return null;
    }

    @Override
    public Optional<ProductModel> update(Long id, ProductDTO productDTO) {
        return Optional.empty();
    }

    @Override
    public List<ProductModel> updateAll(Map<Long, ProductDTO> longProductDTOMap) {
        return null;
    }

    @Override
    public boolean deleleById(Long id) {
        return false;
    }

    @Override
    public boolean deleteAll(List<Long> ids) {
        return false;
    }

    @Override
    public List<ProductModel> search(String keywords) {
        return null;
    }

    @Override
    public ProductModel convertToModel(Product product) {
        return new ProductModel(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getSalePrice(),
                product.getSKU(),
                product.getThumbnail(),
                product.getIframeVideo(),
                product.getViewCount(),
                product.getWishlistCount(),
                product.getCategory().getId(),
                product.getLabel().getId(),
                product.getDiscount(),
                product.getSeoTitle(),
                product.getMetaKeys(),
                product.getMetaDesc(),
                DateTimeUtils.formatLocalDateTime(product.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(product.getUpdatedAt())
        );
    }


    @Override
    public void convertToEntity(ProductDTO productDTO, Product product) {
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setSKU(productDTO.getSKU());
        product.setThumbnail(productDTO.getThumbnail());
        product.setViewCount(productDTO.getViewCount());
        product.setWishlistCount(productDTO.getWishlistCount());
        product.setCategory(categoryRepository.findById(productDTO.getCategoryId()).get());
        product.setLabel(labelRepository.findById(productDTO.getLabelId()).get());
        product.setDiscount(productDTO.getDiscount());
        product.setIframeVideo(productDTO.getIframeVideo());
        product.setSeoTitle(productDTO.getSeoTitle());
        product.setMetaKeys(productDTO.getMetaKeys());
        product.setMetaDesc(productDTO.getMetaDesc());
    }
}
