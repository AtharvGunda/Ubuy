package com.ubuy.service;

import com.ubuy.dto.cart.AddToCartRequest;
import com.ubuy.dto.cart.CartItemResponse;
import com.ubuy.dto.cart.CartResponse;
import com.ubuy.entity.Cart;
import com.ubuy.entity.CartItem;
import com.ubuy.entity.Product;
import com.ubuy.entity.User;
import com.ubuy.exception.BadRequestException;
import com.ubuy.exception.ResourceNotFoundException;
import com.ubuy.repository.CartItemRepository;
import com.ubuy.repository.CartRepository;
import com.ubuy.repository.ProductRepository;
import com.ubuy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String userEmail, AddToCartRequest req) {
        User user = getUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + req.getProductId()));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int requestedQty = req.getQuantity();
        int currentQty = existingItem.map(CartItem::getQuantity).orElse(0);
        int totalQty = currentQty + requestedQty;

        if (totalQty > product.getStockQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock for: " + product.getName() +
                    ". Available: " + product.getStockQuantity());
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(totalQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(requestedQty);
            cartItemRepository.save(item);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        return toResponse(updatedCart);
    }

    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        User user = getUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        cartItemRepository.delete(item);
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        return toResponse(updatedCart);
    }

    @Transactional
    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), itemResponses, total);
    }
}

