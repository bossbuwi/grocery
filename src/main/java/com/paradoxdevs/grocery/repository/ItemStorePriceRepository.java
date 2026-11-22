package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.ItemStorePrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStorePriceRepository extends JpaRepository<ItemStorePrice, Long> {
    void deleteByItemId(Long itemId);

    void deleteByStoreId(Long storeId);
}
