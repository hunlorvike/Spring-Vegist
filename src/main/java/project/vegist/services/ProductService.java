package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.dtos.ProductDTO;
import project.vegist.entities.Product;
import project.vegist.entities.ProductImage;
import project.vegist.entities.ProductUnit;
import project.vegist.entities.Unit;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ProductImageModel;
import project.vegist.models.ProductModel;
import project.vegist.models.ProductUnitModel;
import project.vegist.repositories.*;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.FileUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService implements CrudService<Product, ProductDTO, ProductModel> {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;
    private final ProductUnitRepository productUnitRepository;
    private final UnitRepository unitRepository;
    private final FileUtils fileUtils;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository, LabelRepository labelRepository, FileUtils fileUtils, ProductUnitRepository productUnitRepository, UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
        this.fileUtils = fileUtils;
        this.productUnitRepository = productUnitRepository;
        this.unitRepository = unitRepository;
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
        return productRepository.findAll(pageable).getContent().stream()
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
    public Optional<ProductModel> create(ProductDTO productDTO) throws IOException {
        try {
            // Save the new product
            String thumbnailFileName = fileUtils.uploadFile(productDTO.getThumbnail(), true);
            Product newProduct = new Product();
            convertToEntity(productDTO, newProduct);
            newProduct.setThumbnail(thumbnailFileName);
            newProduct = productRepository.save(newProduct);

            final Product finalProduct = newProduct;

            // Save the product units
            List<ProductUnit> productUnits = productDTO.getUnitIds().stream()
                    .map(unitId -> new ProductUnit(finalProduct, getUnitById(unitId)))
                    .collect(Collectors.toList());

            productUnitRepository.saveAll(productUnits);

            // Save the product images
            List<ProductImage> productImages = productDTO.getImagesProduct().stream()
                    .map(productFile -> {
                        try {
                            String productFileName = fileUtils.uploadFile(productFile, true);
                            if (productFileName != null) {
                                return new ProductImage(finalProduct, productFileName);
                            } else {
                                throw new RuntimeException("Failed to upload product image: " + productFile.getOriginalFilename());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            productImageRepository.saveAll(productImages);

            return Optional.ofNullable(convertToModel(finalProduct));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty(); // or throw a custom exception with a meaningful message
        }
    }


    private Unit getUnitById(Long unitId) {
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new NoSuchElementException("Unit not found"));
    }

    @Override
    @Transactional
    public List<ProductModel> createAll(List<ProductDTO> productDTOS) throws IOException {
        List<ProductModel> createdProducts = new ArrayList<>();

        for (ProductDTO productDTO : productDTOS) {
            String thumbnailFileName = fileUtils.uploadFile(productDTO.getThumbnail(), true);
            Product newProduct = new Product();
            convertToEntity(productDTO, newProduct);
            newProduct.setThumbnail(thumbnailFileName);
            newProduct = productRepository.save(newProduct);

            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile productFile : productDTO.getImagesProduct()) {
                String productFileName = fileUtils.uploadFile(productFile, true);

                ProductImage productImage = new ProductImage();
                productImage.setProduct(newProduct);
                productImage.setImagePath(productFileName);
                productImages.add(productImage);
            }

            productImageRepository.saveAll(productImages);

            ProductModel createdProductModel = convertToModel(newProduct);
            createdProducts.add(createdProductModel);
        }

        return createdProducts;
    }

    @Override
    @Transactional
    public Optional<ProductModel> update(Long id, ProductDTO productDTO) {
        return productRepository.findById(id).map(existingProduct -> {
            convertToEntity(productDTO, existingProduct);

            List<ProductUnit> existingProductUnits = existingProduct.getProductUnits();

            // Identify units to be removed
            Set<Long> updatedUnitIds = new HashSet<>(productDTO.getUnitIds());
            Set<Long> existingUnitIds = existingProductUnits.stream()
                    .map(productUnit -> productUnit.getUnit().getId())
                    .collect(Collectors.toSet());

            Set<Long> unitsToRemove = existingUnitIds.stream()
                    .filter(unitId -> !updatedUnitIds.contains(unitId))
                    .collect(Collectors.toSet());

            // Remove unwanted units from the repository
            existingProductUnits.removeIf(productUnit -> unitsToRemove.contains(productUnit.getUnit().getId()));
            unitsToRemove.forEach(unitId -> {
                Unit unitToRemove = unitRepository.findById(unitId)
                        .orElseThrow(() -> new ResourceNotFoundException("Unit", unitId, HttpStatus.CONFLICT));
                ProductUnit productUnitToRemove = new ProductUnit(existingProduct, unitToRemove);
                productUnitRepository.delete(productUnitToRemove);
            });

            // Identify units to be added
            List<ProductUnit> unitsToAdd = updatedUnitIds.stream()
                    .filter(unitId -> !existingUnitIds.contains(unitId))
                    .map(unitId -> {
                        Unit unit = getUnitById(unitId);
                        return new ProductUnit(existingProduct, unit);
                    })
                    .collect(Collectors.toList());

            // Save the new units
            productUnitRepository.saveAll(unitsToAdd);

            // Logic for handling product images
            List<ProductImage> existingProductImages = existingProduct.getProductImages();

            // Identify images to be removed
            Set<String> existingImageFileNames = existingProductImages.stream()
                    .map(productImage -> FileUtils.getFileNameFromUrl(productImage.getImagePath()))
                    .collect(Collectors.toSet());

            Set<String> updatedImageFileNames = productDTO.getImagesProduct().stream()
                    .map(productFile -> {
                        try {
                            return FileUtils.generateUniqueFileName(productFile.getOriginalFilename());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());

            Set<String> imagesToRemove = existingImageFileNames.stream()
                    .filter(fileName -> !updatedImageFileNames.contains(fileName))
                    .collect(Collectors.toSet());

            // Remove unwanted images from the repository
            existingProductImages.removeIf(productImage -> imagesToRemove.contains(FileUtils.getFileNameFromUrl(productImage.getImagePath())));
            existingProductImages.forEach(productImage -> productImageRepository.deleteByProduct_IdAndImagePath(id, productImage.getImagePath()));

            // Identify images to be added
            List<ProductImage> imagesToAdd = productDTO.getImagesProduct().stream()
                    .filter(productFile -> {
                        try {
                            return !existingImageFileNames.contains(FileUtils.generateUniqueFileName(Objects.requireNonNull(productFile.getOriginalFilename())));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(productFile -> {
                        try {
                            // Ensure the complete URL is stored in the database
                            String productFileName = fileUtils.uploadFile(productFile, true);
                            return new ProductImage(existingProduct, productFileName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // Save the new images
            productImageRepository.saveAll(imagesToAdd);

            // Save changes to the product
            Product updatedProduct = productRepository.save(existingProduct);

            return convertToModel(updatedProduct);
        });
    }

    @Override
    @Transactional
    public List<ProductModel> updateAll(Map<Long, ProductDTO> longProductDTOMap) {
        List<ProductModel> updatedProducts = new ArrayList<>();

        for (Map.Entry<Long, ProductDTO> entry : longProductDTOMap.entrySet()) {
            Long productId = entry.getKey();
            ProductDTO productDTO = entry.getValue();

            Optional<ProductModel> updatedProduct = update(productId, productDTO);
            updatedProduct.ifPresent(updatedProducts::add);
        }

        return updatedProducts;
    }


    @Override
    @Transactional
    public boolean deleteById(Long id) {
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
    @Transactional(readOnly = true)
    public List<ProductModel> search(String keywords) {
        SpecificationsBuilder<Product> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("productName", keywords);
                        builder.like("description", keywords);
                        builder.like("sku", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Product> spec = specificationsBuilder.build();

        return productRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public ProductModel convertToModel(Product product) {
        List<ProductImage> productImages = productImageRepository.findByProduct_Id(product.getId());
        List<ProductImageModel> productImageModels = productImages.stream()
                .map(productImage -> new ProductImageModel(productImage.getId(), productImage.getProduct().getId(), productImage.getImagePath()))
                .collect(Collectors.toList());

        List<ProductUnit> productUnits = productUnitRepository.findByProduct_Id(product.getId());
        List<ProductUnitModel> productUnitModels = productUnits.stream()
                .map(productUnit -> new ProductUnitModel(productUnit.getId(), productUnit.getProduct().getId(), productUnit.getUnit().getId()))
                .collect(Collectors.toList());

        return new ProductModel(
                product.getId(), product.getProductName(), product.getDescription(), product.getPrice(), product.getSalePrice(),
                product.getSKU(), product.getThumbnail(), product.getIframeVideo(), product.getViewCount(), product.getWishlistCount(),
                product.getCategory().getId(), product.getLabel().getId(), product.getDiscount(), product.getSeoTitle(),
                product.getMetaKeys(), product.getMetaDesc(), DateTimeUtils.formatLocalDateTime(product.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(product.getUpdatedAt()), productImageModels, productUnitModels);
    }


    @Override
    public void convertToEntity(ProductDTO productDTO, Product product) {
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setSKU(productDTO.getSKU());
        product.setViewCount(productDTO.getViewCount());
        product.setWishlistCount(productDTO.getWishlistCount());

        product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found")));

        product.setLabel(labelRepository.findById(productDTO.getLabelId())
                .orElseThrow(() -> new NoSuchElementException("Label not found")));

        product.setDiscount(productDTO.getDiscount());
        product.setIframeVideo(productDTO.getIframeVideo());
        product.setSeoTitle(productDTO.getSeoTitle());
        product.setMetaKeys(productDTO.getMetaKeys());
        product.setMetaDesc(productDTO.getMetaDesc());
    }

}
