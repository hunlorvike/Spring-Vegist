package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.InventoryDTO;
import project.vegist.entities.Inventory;
import project.vegist.entities.Product;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.InventoryModel;
import project.vegist.repositories.InventoryRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService implements CrudService<Inventory, InventoryDTO, InventoryModel> {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryModel> findAll() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryModel> findById(Long id) {
        return inventoryRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<InventoryModel> create(InventoryDTO inventoryDTO) throws IOException {
        Objects.requireNonNull(inventoryDTO, "inventoryDTO must not be null");
        Objects.requireNonNull(inventoryDTO.getProductId(), "productId must not be null");

        Inventory newInventory = new Inventory();
        convertToEntity(inventoryDTO, newInventory);
        return Optional.ofNullable(convertToModel(inventoryRepository.save(newInventory)));
    }

    @Override
    @Transactional
    public List<InventoryModel> createAll(List<InventoryDTO> inventoryDTOS) throws IOException {
        List<Inventory> newInventories = inventoryDTOS.stream()
                .map(inventoryDTO -> {
                    Objects.requireNonNull(inventoryDTO, "inventoryDTO must not be null");
                    Objects.requireNonNull(inventoryDTO.getProductId(), "productId must not be null");

                    Inventory newInventory = new Inventory();
                    convertToEntity(inventoryDTO, newInventory);
                    return newInventory;
                })
                .collect(Collectors.toList());

        return inventoryRepository.saveAll(newInventories).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<InventoryModel> update(Long id, InventoryDTO inventoryDTO) {
        return inventoryRepository.findById(id)
                .map(existingInventory -> {
                    Objects.requireNonNull(inventoryDTO, "inventoryDTO must not be null");
                    Objects.requireNonNull(inventoryDTO.getProductId(), "productId must not be null");

                    convertToEntity(inventoryDTO, existingInventory);
                    return convertToModel(inventoryRepository.save(existingInventory));
                });
    }

    @Override
    @Transactional
    public List<InventoryModel> updateAll(Map<Long, InventoryDTO> longInventoryDTOMap) {
        List<Inventory> inventoriesToUpdate = inventoryRepository.findAllById(longInventoryDTOMap.keySet());

        inventoriesToUpdate.forEach(existingInventory -> {
            InventoryDTO inventoryDTO = longInventoryDTOMap.get(existingInventory.getId());
            if (inventoryDTO != null) {
                convertToEntity(inventoryDTO, existingInventory);
            }
        });

        List<Inventory> updatedInventories = inventoryRepository.saveAll(inventoriesToUpdate);

        return updatedInventories.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Inventory> inventoriesToDelete = inventoryRepository.findAllById(ids);
        inventoryRepository.deleteAll(inventoriesToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryModel> search(String keywords) {
        SpecificationsBuilder<Inventory> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .like("product.name", keywords) // Assuming product has a "name" field
                    .or(builder -> {
                        builder.like("quantity", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Inventory> spec = specificationsBuilder.build();

        return inventoryRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }
    @Override
    public InventoryModel convertToModel(Inventory inventory) {
        return new InventoryModel(inventory.getId(), inventory.getProduct().getId(), inventory.getQuantity(),
                DateTimeUtils.formatLocalDateTime(inventory.getCreatedAt()), DateTimeUtils.formatLocalDateTime(inventory.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(InventoryDTO inventoryDTO, Inventory inventory) {
        Objects.requireNonNull(inventoryDTO, "inventoryDTO must not be null");
        Objects.requireNonNull(inventory, "inventory must not be null");
        Objects.requireNonNull(inventoryDTO.getProductId(), "productId must not be null");

        inventory.setProduct(productRepository.findById(inventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", inventoryDTO.getProductId(), HttpStatus.NOT_FOUND)));
        inventory.setQuantity(inventoryDTO.getQuantity());
    }
}
