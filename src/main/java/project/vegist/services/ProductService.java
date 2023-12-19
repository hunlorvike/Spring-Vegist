package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.vegist.dtos.ProductDTO;
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
import project.vegist.utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService implements CrudService<Product, ProductDTO, ProductModel> {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;
    private final FileUtils fileUtils;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository, LabelRepository labelRepository, FileUtils fileUtils) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
        this.fileUtils = fileUtils;
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
        String thumbnailFileName = fileUtils.uploadFile(productDTO.getThumbnail());
        Product newProduct = new Product();
        convertToEntity(productDTO, newProduct);
        newProduct.setThumbnail(thumbnailFileName);
        newProduct = productRepository.save(newProduct);

        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile productFile : productDTO.getImagesProduct()) {
            String productFileName = fileUtils.uploadFile(productFile);

            ProductImage productImage = new ProductImage();
            productImage.setProduct(newProduct);
            productImage.setImagePath(productFileName);
            productImages.add(productImage);
        }

        productImageRepository.saveAll(productImages);

        return Optional.ofNullable(convertToModel(newProduct));
    }

    @Override
    @Transactional
    public List<ProductModel> createAll(List<ProductDTO> productDTOS) throws IOException {
        List<ProductModel> createdProducts = new ArrayList<>();

        for (ProductDTO productDTO : productDTOS) {
            String thumbnailFileName = fileUtils.uploadFile(productDTO.getThumbnail());
            Product newProduct = new Product();
            convertToEntity(productDTO, newProduct);
            newProduct.setThumbnail(thumbnailFileName);
            newProduct = productRepository.save(newProduct);

            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile productFile : productDTO.getImagesProduct()) {
                String productFileName = fileUtils.uploadFile(productFile);

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

            // Update or add new images
            List<ProductImage> existingImages = existingProduct.getProductImages();
            List<ProductImage> newImages = productDTO.getImagesProduct().stream()
                    .map(productFile -> {
                        try {
                            return new ProductImage(existingProduct, fileUtils.uploadFile(productFile));
                        } catch (IOException e) {
                            throw new RuntimeException("Error uploading files: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());

            // Identify and remove unwanted images
            Set<String> newImageFileNames = newImages.stream()
                    .map(ProductImage::getFileName)
                    .collect(Collectors.toSet());

            existingImages.removeIf(image -> !newImageFileNames.contains(image.getFileName()));

            // Remove unwanted images from the repository
            existingImages.forEach(image -> {
                productImageRepository.deleteByProduct_IdAndImagePath(id, image.getImagePath());
            });


            // Save the new images
            productImageRepository.saveAll(newImages);

            // Save changes to the product
            Product updatedProduct = productRepository.save(existingProduct);

            return convertToModel(updatedProduct);
        });
    }


    @Override
    @Transactional
    public List<ProductModel> updateAll(Map<Long, ProductDTO> longProductDTOMap) {
        return null;
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
    public List<ProductModel> search(String keywords) {
        return null;
    }

    @Override
    public ProductModel convertToModel(Product product) {
        List<ProductImage> productImages = productImageRepository.findByProduct_Id(product.getId());
        List<ProductImageModel> productImageModels = productImages.stream()
                .map(productImage -> new ProductImageModel(productImage.getId(), productImage.getProduct().getId(), productImage.getImagePath()))
                .collect(Collectors.toList());

        return new ProductModel(
                product.getId(), product.getProductName(), product.getDescription(), product.getPrice(), product.getSalePrice(),
                product.getSKU(), product.getThumbnail(), product.getIframeVideo(), product.getViewCount(), product.getWishlistCount(),
                product.getCategory().getId(), product.getLabel().getId(), product.getDiscount(), product.getSeoTitle(),
                product.getMetaKeys(), product.getMetaDesc(), DateTimeUtils.formatLocalDateTime(product.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(product.getUpdatedAt()), productImageModels);
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

        // Use orElseThrow() to handle the case when Optional is empty
        product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found")));

        // Use orElseThrow() to handle the case when Optional is empty
        product.setLabel(labelRepository.findById(productDTO.getLabelId())
                .orElseThrow(() -> new NoSuchElementException("Label not found")));

        product.setDiscount(productDTO.getDiscount());
        product.setIframeVideo(productDTO.getIframeVideo());
        product.setSeoTitle(productDTO.getSeoTitle());
        product.setMetaKeys(productDTO.getMetaKeys());
        product.setMetaDesc(productDTO.getMetaDesc());
    }

}
