package com.ubuy.service;

import com.ubuy.dto.product.ProductRequest;
import com.ubuy.dto.product.ProductResponse;
import com.ubuy.entity.Product;
import com.ubuy.exception.ResourceNotFoundException;
import com.ubuy.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductService productService;

    @Test
    void getProductById_throwsNotFound_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createProduct_returnsSavedProduct() {
        ProductRequest req = new ProductRequest("Shirt", "Cotton shirt",
                new BigDecimal("29.99"), 100, "Apparel");
        Product saved = new Product(1L, "Shirt", "Cotton shirt",
                new BigDecimal("29.99"), 100, "Apparel");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse resp = productService.createProduct(req);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getPrice()).isEqualByComparingTo("29.99");
        assertThat(resp.getName()).isEqualTo("Shirt");
    }

    @Test
    void updateProduct_throwsNotFound_whenMissing() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());
        ProductRequest req = new ProductRequest("X", "Y", BigDecimal.ONE, 1, "Z");

        assertThatThrownBy(() -> productService.updateProduct(5L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void deleteProduct_throwsNotFound_whenMissing() {
        when(productRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

