package com.ubuy.repository;

import com.ubuy.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findById(Long id);
}
