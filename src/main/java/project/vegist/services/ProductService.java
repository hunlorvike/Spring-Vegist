package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.vegist.dtos.ProductDTO;
import project.vegist.entities.Product;
import project.vegist.models.ProductImageModel;
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
    @Override
    public List<ProductModel> findAll() {
        return null;
    }

    @Override
    public List<ProductModel> findAll(int page, int size) {
        return null;
    }

    @Override
    public Optional<ProductModel> findById(Long id) {
        return Optional.empty();
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
        return null;
    }

    @Override
    public void convertToEntity(ProductDTO productDTO, Product product) {

    }
}


