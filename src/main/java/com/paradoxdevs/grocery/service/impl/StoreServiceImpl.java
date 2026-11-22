package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.StoreRequest;
import com.paradoxdevs.grocery.api.response.StoreResponse;
import com.paradoxdevs.grocery.dto.StoreDto;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.repository.ItemStorePriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final ItemStorePriceRepository itemStorePriceRepository;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            ItemStorePriceRepository itemStorePriceRepository
    ) {
        this.storeRepository = storeRepository;
        this.itemStorePriceRepository = itemStorePriceRepository;
    }

    @Override
    public StoreResponse create(StoreRequest storeRequest) {
        Store store = new Store();
        store.setName(storeRequest.getName());
        return toResponse(toDto(storeRepository.save(store)));
    }

    @Override
    public Optional<StoreResponse> updateById(Long id, StoreRequest storeRequest) {
        return storeRepository.findById(id)
                .map(store -> {
                    store.setName(storeRequest.getName());
                    return storeRepository.save(store);
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

        itemStorePriceRepository.deleteByStoreId(id);
        storeRepository.delete(store.get());
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
