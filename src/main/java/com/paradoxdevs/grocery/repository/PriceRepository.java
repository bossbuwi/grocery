package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {
    boolean existsByItemIdAndStoreId(Long itemId, Long storeId);

    boolean existsByItemIdAndStoreIdAndIdNot(Long itemId, Long storeId, Long id);

    long countByStoreId(Long storeId);

    void deleteByItemId(Long itemId);
}
