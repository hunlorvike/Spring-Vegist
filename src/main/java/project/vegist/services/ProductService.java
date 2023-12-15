package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.ProductDTO;
import project.vegist.dtos.ProductImageDTO;
import project.vegist.entities.Product;
import project.vegist.entities.ProductImage;
import project.vegist.models.ProductImageModel;
import project.vegist.models.ProductModel;
import project.vegist.repositories.CategoryRepository;
import project.vegist.repositories.LabelRepository;
import project.vegist.repositories.ProductImageRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService implements CrudService<Product, ProductDTO, ProductModel> {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductModel> findAll() {
        return productRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductModel> findById(Long id) {
        return productRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<ProductModel> create(ProductDTO productDTO) {
        Product product = new Product();
        convertToEntity(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return Optional.of(convertToModel(savedProduct));
    }

    @Override
    @Transactional
    public List<ProductModel> createAll(List<ProductDTO> productDTOS) {
        List<Product> products = productDTOS.stream()
                .map(productDTO -> {
                    Product product = new Product();
                    convertToEntity(productDTO, product);
                    return product;
                })
                .collect(Collectors.toList());

        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ProductModel> update(Long id, ProductDTO productDTO) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            convertToEntity(productDTO, product);
            Product savedProduct = productRepository.save(product);
            return Optional.of(convertToModel(savedProduct));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public List<ProductModel> updateAll(Map<Long, ProductDTO> longProductDTOMap) {
        List<Product> products = longProductDTOMap.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    ProductDTO productDTO = entry.getValue();
                    Optional<Product> optionalProduct = productRepository.findById(productId);
                    if (optionalProduct.isPresent()) {
                        Product product = optionalProduct.get();
                        convertToEntity(productDTO, product);
                        return product;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleleById(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Product> productsToDelete = productRepository.findAllById(ids);
        productRepository.deleteAll(productsToDelete);
        return true;
    }

    @Override
    @Transactional
    public List<ProductModel> search(String keywords) {
        return null;
    }

    @Override
    public ProductModel convertToModel(Product product) {
        ProductModel productModel = new ProductModel();

        productModel.setId(product.getId());
        productModel.setProductName(product.getProductName());
        productModel.setDescription(product.getDescription());
        productModel.setPrice(product.getPrice());
        productModel.setSalePrice(product.getSalePrice());
        productModel.setSKU(product.getSKU());
        productModel.setThumbnail(product.getThumbnail());
        productModel.setIframeVideo(product.getIframeVideo());
        productModel.setViewCount(product.getViewCount());
        productModel.setWishlistCount(product.getWishlistCount());
        productModel.setCategoryId(product.getCategory().getId());
        productModel.setLabelId(product.getLabel().getId());
        productModel.setDiscount(product.getDiscount());
        productModel.setSeoTitle(product.getSeoTitle());
        productModel.setMetaKeys(product.getMetaKeys());
        productModel.setMetaDesc(product.getMetaDesc());
        productModel.setCreatedAt(DateTimeUtils.formatLocalDateTime(product.getCreatedAt()));
        productModel.setUpdatedAt(DateTimeUtils.formatLocalDateTime(product.getUpdatedAt()));

        // Convert and set other relationships
        productModel.setProductImages(convertProductImagesToModels(product.getProductImages()));

        return productModel;
    }

    @Override
    public void convertToEntity(ProductDTO productDTO, Product product) {
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setSKU(productDTO.getSKU());
        product.setThumbnail(productDTO.getThumbnail());
        product.setIframeVideo(productDTO.getIframeVideo());
        product.setViewCount(productDTO.getViewCount());
        product.setWishlistCount(productDTO.getWishlistCount());

        // Set Category and Label based on IDs from DTO
        product.setCategory(categoryRepository.findById(productDTO.getCategoryId()).orElse(null));
        product.setLabel(labelRepository.findById(productDTO.getLabelId()).orElse(null));

        product.setDiscount(productDTO.getDiscount());
        product.setSeoTitle(productDTO.getSeoTitle());
        product.setMetaKeys(productDTO.getMetaKeys());
        product.setMetaDesc(productDTO.getMetaDesc());

        // Convert and set other relationships
        product.setProductImages(convertProductImageDTOsToEntities(productDTO.getImagesProduct(), product));
        // Set other relationships in a similar manner
    }

    private List<ProductImageModel> convertProductImagesToModels(List<ProductImage> productImages) {
        return productImages.stream()
                .map(productImage -> {
                    ProductImageModel imageModel = new ProductImageModel();
                    imageModel.setId(productImage.getId());
                    imageModel.setProductId(productImage.getProduct().getId());
                    imageModel.setImagePath(productImage.getImagePath());
                    return imageModel;
                })
                .collect(Collectors.toList());
    }

    private List<ProductImage> convertProductImageDTOsToEntities(List<ProductImageDTO> productImageDTOs, Product product) {
        return productImageDTOs.stream()
                .map(imageDTO -> {
                    ProductImage productImage = new ProductImage();
                    productImage.setProduct(product);
                    productImage.setImagePath(imageDTO.getImagePath());
                    return productImage;
                })
                .collect(Collectors.toList());
    }
}
