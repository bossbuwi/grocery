package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.ItemStorePrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStorePriceRepository extends JpaRepository<ItemStorePrice, Long> {
    boolean existsByItemIdAndStoreId(Long itemId, Long storeId);

    boolean existsByItemIdAndStoreIdAndIdNot(Long itemId, Long storeId, Long id);

    void deleteByItemId(Long itemId);

    void deleteByStoreId(Long storeId);
}
