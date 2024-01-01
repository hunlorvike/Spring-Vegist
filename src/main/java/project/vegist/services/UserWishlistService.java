package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.UserWishlistDTO;
import project.vegist.entities.Product;
import project.vegist.entities.User;
import project.vegist.entities.UserWishlist;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.UserWishlistModel;
import project.vegist.repositories.ProductRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.repositories.UserWishlistRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserWishlistService implements CrudService<UserWishlist, UserWishlistDTO, UserWishlistModel> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserWishlistRepository userWishlistRepository;

    @Autowired
    public UserWishlistService(UserRepository userRepository, ProductRepository productRepository, UserWishlistRepository userWishlistRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.userWishlistRepository = userWishlistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWishlistModel> findAll() {
        return userWishlistRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWishlistModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userWishlistRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserWishlistModel> findById(Long id) {
        return userWishlistRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<UserWishlistModel> create(UserWishlistDTO userWishlistDTO) throws IOException {
        UserWishlist newUserWishlist = new UserWishlist();
        convertToEntity(userWishlistDTO, newUserWishlist);
        return Optional.ofNullable(convertToModel(userWishlistRepository.save(newUserWishlist)));
    }

    @Override
    @Transactional
    public List<UserWishlistModel> createAll(List<UserWishlistDTO> userWishlistDTOS) throws IOException {
        List<UserWishlist> newUserWishlists = userWishlistDTOS.stream()
                .map(userWishlistDTO -> {
                    UserWishlist newUserWishlist = new UserWishlist();
                    convertToEntity(userWishlistDTO, newUserWishlist);
                    return newUserWishlist;
                })
                .collect(Collectors.toList());

        return userWishlistRepository.saveAll(newUserWishlists)
                .stream().map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UserWishlistModel> update(Long id, UserWishlistDTO userWishlistDTO) {
        return userWishlistRepository.findById(id)
                .map(existingUserWishlist -> {
                    convertToEntity(userWishlistDTO, existingUserWishlist);
                    return convertToModel(userWishlistRepository.save(existingUserWishlist));
                });
    }

    @Override
    @Transactional
    public List<UserWishlistModel> updateAll(Map<Long, UserWishlistDTO> longUserWishlistDTOMap) {
        List<UserWishlist> userWishlistsToUpdate = userWishlistRepository.findAllById(longUserWishlistDTOMap.keySet());

        userWishlistsToUpdate.forEach(existingUserWishlist -> {
            UserWishlistDTO wishlistDTO = longUserWishlistDTOMap.get(existingUserWishlist.getId());
            if (wishlistDTO != null) {
                convertToEntity(wishlistDTO, existingUserWishlist);
            }
        });

        return userWishlistRepository.saveAll(userWishlistsToUpdate)
                .stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (userWishlistRepository.existsById(id)) {
            userWishlistRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<UserWishlist> userWishlistsToDelete = userWishlistRepository.findAllById(ids);
        userWishlistRepository.deleteAll(userWishlistsToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWishlistModel> search(String keywords) {
        SpecificationsBuilder<UserWishlist> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("user.fullName", keywords); // Assuming you have a 'fullName' field in the 'User' entity associated with the UserWishlist
                        builder.like("product.productName", keywords); // Assuming you have a 'productName' field in the 'Product' entity associated with the UserWishlist
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<UserWishlist> spec = specificationsBuilder.build();

        List<UserWishlistModel> result = userWishlistRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public UserWishlistModel convertToModel(UserWishlist userWishlist) {
        return new UserWishlistModel(userWishlist.getId(),
                userWishlist.getUser() != null ? userWishlist.getUser().getId() : null,
                userWishlist.getProduct() != null ? userWishlist.getProduct().getId() : null,
                DateTimeUtils.formatLocalDateTime(userWishlist.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(userWishlist.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(UserWishlistDTO userWishlistDTO, UserWishlist userWishlist) {
        if (userWishlistDTO.getUserId() != null) {
            User user = userRepository.findById(userWishlistDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", userWishlistDTO.getUserId(), HttpStatus.NOT_FOUND));
            userWishlist.setUser(user);
        }

        if (userWishlistDTO.getProductId() != null) {
            Product product = productRepository.findById(userWishlistDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", userWishlistDTO.getProductId(), HttpStatus.NOT_FOUND));
            userWishlist.setProduct(product);
        }
    }
}
