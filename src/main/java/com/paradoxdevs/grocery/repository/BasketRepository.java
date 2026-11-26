package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.Basket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {
    @Override
    @EntityGraph(attributePaths = {"store", "items", "items.price", "items.price.item"})
    List<Basket> findAll();

    @Override
    @EntityGraph(attributePaths = {"store", "items", "items.price", "items.price.item"})
    Optional<Basket> findById(Long id);

    long countByStoreId(Long storeId);
}
