package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.CartDTO;
import project.vegist.dtos.CartItemDTO;
import project.vegist.entities.Cart;
import project.vegist.entities.CartItem;
import project.vegist.entities.User;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.CartItemModel;
import project.vegist.models.CartModel;
import project.vegist.repositories.CartItemRepository;
import project.vegist.repositories.CartRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static project.vegist.enums.CartStatus.PENDING;

@Service
public class CartService implements CrudService<Cart, CartDTO, CartModel> {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CartModel> findAll() {
        return cartRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cartRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CartModel> findById(Long id) {
        return cartRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<CartModel> create(CartDTO cartDTO) throws IOException {
        Objects.requireNonNull(cartDTO, "cartDTO must not be null");

        Long userId = Objects.requireNonNull(cartDTO.getUserId(), "User ID must not be null");
        Optional<CartModel> pendingCart = findPendingCartByUserId(userId);

        if (pendingCart.isPresent()) {
            return pendingCart.flatMap(cartModel -> update(cartModel.getId(), cartDTO));
        } else {
            // Người dùng chưa có giỏ hàng PENDING, tạo một giỏ hàng mới
            Cart newCart = new Cart();
            convertToEntity(cartDTO, newCart);
            newCart.setStatus(PENDING);
            newCart = cartRepository.save(newCart);
            return Optional.ofNullable(convertToModel(newCart));
        }
    }

    @Override
    @Transactional
    public List<CartModel> createAll(List<CartDTO> cartDTOS) throws IOException {
        List<CartModel> createdCarts = new ArrayList<>();

        for (CartDTO cartDTO : cartDTOS) {
            Cart newCart = new Cart();
            convertToEntity(cartDTO, newCart);
            newCart = cartRepository.save(newCart);
            CartModel createdCartModel = convertToModel(newCart);
            createdCarts.add(createdCartModel);
        }

        return createdCarts;
    }

    @Override
    @Transactional
    public Optional<CartModel> update(Long id, CartDTO cartDTO) {
        return cartRepository.findById(id).map(existingCart -> {
            updateCartItems(existingCart, cartDTO.getCartItems());

            Cart updatedCart = cartRepository.save(existingCart);

            return convertToModel(updatedCart);
        });
    }

    private void updateCartItems(Cart cart, List<CartItemDTO> cartItemDTOs) {
        for (CartItemDTO cartItemDTO : cartItemDTOs) {
            Long productId = cartItemDTO.getProductId();
            Optional<CartItem> existingCartItem = findCartItemByProductId(cart.getCartItems(), productId);

            if (existingCartItem.isPresent()) {
                updateCartItemByDTO(existingCartItem.get(), cartItemDTO);
            } else {
                CartItem newCartItem = convertCartItemDTOToEntity(cartItemDTO, cart);
                cart.getCartItems().add(newCartItem);
            }
        }

        cart.getCartItems().removeIf(cartItem -> !containsCartItemByProductId(cartItemDTOs, cartItem.getProduct().getId()));
    }

    private Optional<CartItem> findCartItemByProductId(List<CartItem> cartItems, Long productId) {
        return cartItems.stream()
                .filter(cartItem -> Objects.equals(cartItem.getProduct().getId(), productId))
                .findFirst();
    }


    private void updateCartItemByDTO(CartItem cartItem, CartItemDTO cartItemDTO) {
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(cartItemDTO.getPrice());
    }


    private boolean containsCartItemByProductId(List<CartItemDTO> cartItemDTOs, Long productId) {
        return cartItemDTOs.stream().anyMatch(cartItemDTO -> Objects.equals(cartItemDTO.getProductId(), productId));
    }

    @Override
    @Transactional
    public List<CartModel> updateAll(Map<Long, CartDTO> longCartDTOMap) {
        List<CartModel> updatedCarts = new ArrayList<>();

        for (Map.Entry<Long, CartDTO> entry : longCartDTOMap.entrySet()) {
            Long cartId = entry.getKey();
            CartDTO cartDTO = entry.getValue();

            cartRepository.findById(cartId).ifPresent(existingCart -> {
                convertToEntity(cartDTO, existingCart);
                Cart updatedCart = cartRepository.save(existingCart);
                updatedCarts.add(convertToModel(updatedCart));
            });
        }

        return updatedCarts;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        int deletedCartItems = cartItemRepository.deleteCartItemsByCartId(id);
        int deletedCart = cartRepository.deleteCartById(id);

        return deletedCartItems > 0 && deletedCart > 0;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        int cartItemsDeleted = cartItemRepository.deleteCartItemsByCartIds(ids);
        int cartsDeleted = cartRepository.deleteAllCartById(ids);

        return cartItemsDeleted > 0 && cartsDeleted > 0;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CartModel> search(String keywords) {
        SpecificationsBuilder<Cart> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .like("status", keywords) // Add more fields if needed
                    .or(builder -> {

                    });
        }

        return cartRepository.findAll(specificationsBuilder.build()).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public CartModel convertToModel(Cart cart) {
        List<CartItemModel> cartItemModels = cart.getCartItems().stream()
                .map(this::convertCartItemToModel)
                .collect(Collectors.toList());

        return new CartModel(
                cart.getId(),
                cart.getUser().getId(),
                cartItemModels,
                cart.getStatus(),
                DateTimeUtils.formatLocalDateTime(cart.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(cart.getUpdatedAt())
        );
    }

    private CartItemModel convertCartItemToModel(CartItem cartItem) {
        return new CartItemModel(
                cartItem.getId(),
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity(),
                cartItem.getPrice(),
                DateTimeUtils.formatLocalDateTime(cartItem.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(cartItem.getUpdatedAt())
        );
    }

    @Override
    @Transactional
    public void convertToEntity(CartDTO cartDTO, Cart cart) {
        Objects.requireNonNull(cartDTO, "cartDTO must not be null");
        Objects.requireNonNull(cart, "cart must not be null");

        Long userId = Objects.requireNonNull(cartDTO.getUserId(), "User ID must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        cart.setUser(user);

        List<CartItem> cartItems = Optional.ofNullable(cartDTO.getCartItems())
                .orElse(Collections.emptyList())
                .stream()
                .map(cartItemDTO -> convertCartItemDTOToEntity(cartItemDTO, cart))
                .collect(Collectors.toList());

        cart.setCartItems(cartItems);
    }

    private CartItem convertCartItemDTOToEntity(CartItemDTO cartItemDTO, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found")));
        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItem.setPrice(cartItemDTO.getPrice());
        return cartItem;
    }


    public Optional<CartModel> findPendingCartByUserId(Long userId) {
        Objects.requireNonNull(userId, "User ID must not be null");

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return cartRepository.findByUserIdAndStatus(userId, PENDING).map(this::convertToModel);
        } else {
            throw new ResourceNotFoundException("User ", userId, HttpStatus.CONFLICT);
        }
    }
}