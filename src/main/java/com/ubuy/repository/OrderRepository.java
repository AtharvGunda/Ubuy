package com.ubuy.repository;

import com.ubuy.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findAll();
}
