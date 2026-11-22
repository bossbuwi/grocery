package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.StoreRequest;
import com.paradoxdevs.grocery.api.response.StoreResponse;
import com.paradoxdevs.grocery.dto.StoreDto;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.exception.ResourceHasChildrenException;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final PriceRepository priceRepository;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            PriceRepository priceRepository
    ) {
        this.storeRepository = storeRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    public StoreResponse create(StoreRequest storeRequest) {
        Store store = new Store();
        store.setName(storeRequest.getName());
        Store savedStore = storeRepository.save(store);
        log.info("Created store id={}", savedStore.getId());
        return toResponse(toDto(savedStore));
    }

    @Override
    public Optional<StoreResponse> updateById(Long id, StoreRequest storeRequest) {
        return storeRepository.findById(id)
                .map(store -> {
                    store.setName(storeRequest.getName());
                    Store updatedStore = storeRepository.save(store);
                    log.info("Updated store id={}", updatedStore.getId());
                    return updatedStore;
                })
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    public Optional<StoreResponse> findById(Long id) {
        return storeRepository.findById(id)
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Optional<Store> store = storeRepository.findById(id);
        if (store.isEmpty()) {
            return false;
        }

        long priceCount = priceRepository.countByStoreId(id);
        if (priceCount > 0) {
            throw new ResourceHasChildrenException(priceCount, "Price");
        }

        storeRepository.delete(store.get());
        log.info("Deleted store id={}", id);
        return true;
    }

    @Override
    public List<StoreResponse> findAll() {
        return storeRepository.findAll()
                .stream()
                .map(this::toDto)
                .map(this::toResponse)
                .toList();
    }

    private StoreDto toDto(Store store) {
        StoreDto storeDto = new StoreDto();
        storeDto.setId(store.getId());
        storeDto.setName(store.getName());
        return storeDto;
    }

    private StoreResponse toResponse(StoreDto storeDto) {
        StoreResponse storeResponse = new StoreResponse();
        storeResponse.setId(storeDto.getId());
        storeResponse.setName(storeDto.getName());
        return storeResponse;
    }
}
