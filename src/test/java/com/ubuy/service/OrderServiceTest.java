package com.ubuy.service;

import com.ubuy.dto.order.OrderResponse;
import com.ubuy.entity.*;
import com.ubuy.exception.BadRequestException;
import com.ubuy.exception.ResourceNotFoundException;
import com.ubuy.repository.*;
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
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock CartService cartService;
    @InjectMocks OrderService orderService;

    private User user;
    private User anotherUser;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@ubuy.com", "hashed", Role.CUSTOMER);
        anotherUser = new User(2L, "Bob", "bob@ubuy.com", "hashed", Role.CUSTOMER);
        cart = new Cart(user);
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        product = new Product(1L, "Shirt", "Cotton", new BigDecimal("29.99"), 10, "Apparel");
    }

    @Test
    void placeOrder_throwsBadRequest_whenCartIsEmpty() {
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder("alice@ubuy.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void placeOrder_snapshotsUnitPrice_andDeductsStock() {
        CartItem item = new CartItem(1L, cart, product, 2);
        cart.setItems(List.of(item));

        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse resp = orderService.placeOrder("alice@ubuy.com");

        assertThat(resp.getTotalAmount()).isEqualByComparingTo("59.98");
        verify(productRepository).save(argThat(p -> p.getStockQuantity() == 8));
        assertThat(resp.getItems().get(0).getUnitPrice()).isEqualByComparingTo("29.99");
        verify(cartService).clearCart("alice@ubuy.com");
    }

    @Test
    void placeOrder_throwsBadRequest_whenInsufficientStock() {
        product.setStockQuantity(1);
        CartItem item = new CartItem(1L, cart, product, 5);
        cart.setItems(List.of(item));

        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder("alice@ubuy.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void getOrderById_throwsNotFound_whenOrderBelongsToAnotherUser() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(anotherUser);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.getOrderById(1L, "alice@ubuy.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

