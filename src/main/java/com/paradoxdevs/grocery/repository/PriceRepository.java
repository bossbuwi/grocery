package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.Price;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findByItemIdAndStoreId(Long itemId, Long storeId);

    @EntityGraph(attributePaths = "item")
    List<Price> findAllByItemIdInAndStoreId(Collection<Long> itemIds, Long storeId);

    boolean existsByItemIdAndStoreId(Long itemId, Long storeId);

    boolean existsByItemIdAndStoreIdAndIdNot(Long itemId, Long storeId, Long id);

    long countByStoreId(Long storeId);

    @EntityGraph(attributePaths = "item")
    List<Price> findAllByStoreIdIn(Collection<Long> storeIds);

    @EntityGraph(attributePaths = "store")
    List<Price> findAllByItemIdIn(Collection<Long> itemIds);

    void deleteByItemId(Long itemId);
}
