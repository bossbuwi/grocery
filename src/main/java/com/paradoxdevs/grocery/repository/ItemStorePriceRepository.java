package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.ItemStorePrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStorePriceRepository extends JpaRepository<ItemStorePrice, Long> {
    void deleteByItem_Id(Long itemId);
}
