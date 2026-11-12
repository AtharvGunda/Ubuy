package com.ubuy.service;

import com.ubuy.dto.cart.AddToCartRequest;
import com.ubuy.dto.cart.CartResponse;
import com.ubuy.entity.Cart;
import com.ubuy.entity.CartItem;
import com.ubuy.entity.Product;
import com.ubuy.entity.User;
import com.ubuy.entity.Role;
import com.ubuy.exception.BadRequestException;
import com.ubuy.exception.ResourceNotFoundException;
import com.ubuy.repository.CartItemRepository;
import com.ubuy.repository.CartRepository;
import com.ubuy.repository.ProductRepository;
import com.ubuy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@ubuy.com", "hashed", Role.CUSTOMER);
        cart = new Cart(user);
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        product = new Product(1L, "Shirt", "Cotton", new BigDecimal("29.99"), 10, "Apparel");
    }

    @Test
    void getCart_createsNewCart_whenNoneExists() {
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CartResponse resp = cartService.getCart("alice@ubuy.com");

        verify(cartRepository).save(any(Cart.class));
        assertThat(resp.getItems()).isEmpty();
        assertThat(resp.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addItem_throwsBadRequest_whenInsufficientStock() {
        product.setStockQuantity(2);
        AddToCartRequest req = new AddToCartRequest(product.getId(), 5);

        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem("alice@ubuy.com", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void removeItem_throwsNotFound_whenItemNotInCart() {
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem("alice@ubuy.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_throwsNotFound_whenItemBelongsToAnotherUsersCart() {
        Cart otherCart = new Cart(new User(2L, "Bob", "bob@ubuy.com", "h", Role.CUSTOMER));
        otherCart.setId(99L);
        CartItem item = new CartItem(10L, otherCart, product, 1);

        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeItem("alice@ubuy.com", 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

