package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.StoreRequest;
import com.paradoxdevs.grocery.api.response.StoreItemResponse;
import com.paradoxdevs.grocery.api.response.StoreReadResponse;
import com.paradoxdevs.grocery.api.response.StoreResponse;
import com.paradoxdevs.grocery.dto.StoreDto;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.exception.ResourceHasChildrenException;
import com.paradoxdevs.grocery.repository.BasketRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final PriceRepository priceRepository;
    private final BasketRepository basketRepository;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            PriceRepository priceRepository,
            BasketRepository basketRepository
    ) {
        this.storeRepository = storeRepository;
        this.priceRepository = priceRepository;
        this.basketRepository = basketRepository;
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
    public Optional<StoreReadResponse> findById(Long id) {
        return storeRepository.findById(id)
                .map(store -> toReadResponses(List.of(store)).getFirst());
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

        long basketCount = basketRepository.countByStoreId(id);
        if (basketCount > 0) {
            throw new ResourceHasChildrenException(basketCount, "Basket");
        }

        storeRepository.delete(store.get());
        log.info("Deleted store id={}", id);
        return true;
    }

    @Override
    public List<StoreReadResponse> findAll() {
        return toReadResponses(storeRepository.findAll());
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

    private List<StoreReadResponse> toReadResponses(List<Store> stores) {
        if (stores.isEmpty()) {
            return List.of();
        }

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();
        Map<Long, List<StoreItemResponse>> itemsByStoreId = priceRepository
                .findAllByStoreIdIn(storeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        price -> price.getStore().getId(),
                        Collectors.mapping(this::toStoreItemResponse, Collectors.toList())
                ));

        return stores.stream()
                .map(store -> {
                    StoreReadResponse response = new StoreReadResponse();
                    response.setId(store.getId());
                    response.setName(store.getName());
                    response.setItems(itemsByStoreId.getOrDefault(store.getId(), List.of()));
                    return response;
                })
                .toList();
    }

    private StoreItemResponse toStoreItemResponse(Price price) {
        StoreItemResponse response = new StoreItemResponse();
        response.setId(price.getItem().getId());
        response.setName(price.getItem().getName());
        response.setBarcode(price.getItem().getBarcode());
        response.setPrice(price.getPrice());
        return response;
    }
}
