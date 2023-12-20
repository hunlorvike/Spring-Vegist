package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.CartDTO;
import project.vegist.dtos.CartItemDTO;
import project.vegist.entities.Cart;
import project.vegist.entities.CartItem;
import project.vegist.models.CartItemModel;
import project.vegist.models.CartModel;
import project.vegist.repositories.CartItemRepository;
import project.vegist.repositories.CartRepository;
import project.vegist.repositories.ProductRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        Cart newCart = new Cart();
        convertToEntity(cartDTO, newCart);
        newCart = cartRepository.save(newCart);
        return Optional.ofNullable(convertToModel(newCart));
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
            convertToEntity(cartDTO, existingCart);
            Cart updatedCart = cartRepository.save(existingCart);
            return convertToModel(updatedCart);
        });
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
    public boolean deleleById(Long id) {
        if (cartRepository.existsById(id)) {
            cartRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Cart> cartsToDelete = cartRepository.findAllById(ids);
        cartRepository.deleteAll(cartsToDelete);
        return true;
    }

    @Override
    public List<CartModel> search(String keywords) {
        return null;
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
    public void convertToEntity(CartDTO cartDTO, Cart cart) {
        cart.setUser(userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found")));

        List<CartItem> cartItems = cartDTO.getCartItems().stream()
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

}