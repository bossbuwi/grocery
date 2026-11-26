package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {
    Optional<BasketItem> findByBasket_IdAndPrice_Item_Id(Long basketId, Long itemId);

    long countByPrice_Id(Long priceId);

    long countByPrice_Item_Id(Long itemId);
}
