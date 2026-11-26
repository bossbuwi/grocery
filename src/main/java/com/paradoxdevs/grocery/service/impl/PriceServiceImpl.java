package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.PriceRequest;
import com.paradoxdevs.grocery.api.response.PriceResponse;
import com.paradoxdevs.grocery.dto.PriceDto;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import com.paradoxdevs.grocery.error.exception.ResourceHasChildrenException;
import com.paradoxdevs.grocery.repository.BasketItemRepository;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {
    private final PriceRepository priceRepository;
    private final BasketItemRepository basketItemRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public PriceServiceImpl(
            PriceRepository priceRepository,
            BasketItemRepository basketItemRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository
    ) {
        this.priceRepository = priceRepository;
        this.basketItemRepository = basketItemRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public Optional<PriceResponse> create(PriceRequest request) {
        Optional<Item> item = itemRepository.findById(request.getItemId());
        Optional<Store> store = storeRepository.findById(request.getStoreId());
        if (item.isEmpty() || store.isEmpty()) {
            return Optional.empty();
        }
        if (priceRepository.existsByItemIdAndStoreId(request.getItemId(), request.getStoreId())) {
            throw duplicatePrice(request);
        }

        Price price = new Price();
        price.setItem(item.get());
        price.setStore(store.get());
        price.setPrice(request.getPrice());

        Price savedPrice = priceRepository.save(price);
        log.info("Created price id={} itemId={} storeId={}",
                savedPrice.getId(), request.getItemId(), request.getStoreId());
        return Optional.of(toResponse(toDto(savedPrice)));
    }

    @Override
    @Transactional
    public Optional<PriceResponse> updateById(Long id, PriceRequest request) {
        Optional<Price> existingPrice = priceRepository.findById(id);
        Optional<Item> item = itemRepository.findById(request.getItemId());
        Optional<Store> store = storeRepository.findById(request.getStoreId());
        if (existingPrice.isEmpty() || item.isEmpty() || store.isEmpty()) {
            return Optional.empty();
        }
        if (priceRepository.existsByItemIdAndStoreIdAndIdNot(
                request.getItemId(), request.getStoreId(), id
        )) {
            throw duplicatePrice(request);
        }

        Price price = existingPrice.get();
        long basketItemCount = basketItemRepository.countByPrice_Id(id);
        if (basketItemCount > 0
                && (!price.getItem().getId().equals(request.getItemId())
                || !price.getStore().getId().equals(request.getStoreId()))) {
            throw new ResourceHasChildrenException(basketItemCount, "BasketItem");
        }
        price.setItem(item.get());
        price.setStore(store.get());
        price.setPrice(request.getPrice());
        Price savedPrice = priceRepository.save(price);
        log.info("Updated price id={} itemId={} storeId={}",
                savedPrice.getId(), request.getItemId(), request.getStoreId());
        return Optional.of(toResponse(toDto(savedPrice)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PriceResponse> findById(Long id) {
        return priceRepository.findById(id)
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Optional<Price> price = priceRepository.findById(id);
        if (price.isEmpty()) {
            return false;
        }

        long basketItemCount = basketItemRepository.countByPrice_Id(id);
        if (basketItemCount > 0) {
            throw new ResourceHasChildrenException(basketItemCount, "BasketItem");
        }

        priceRepository.delete(price.get());
        log.info("Deleted price id={}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> findAll() {
        return priceRepository.findAll()
                .stream()
                .map(this::toDto)
                .map(this::toResponse)
                .toList();
    }

    private ResourceAlreadyExistsException duplicatePrice(PriceRequest request) {
        return new ResourceAlreadyExistsException(
                "price for item/store pair",
                request.getItemId() + "/" + request.getStoreId()
        );
    }

    private PriceDto toDto(Price price) {
        PriceDto dto = new PriceDto();
        dto.setId(price.getId());
        dto.setItemId(price.getItem().getId());
        dto.setStoreId(price.getStore().getId());
        dto.setPrice(price.getPrice());
        return dto;
    }

    private PriceResponse toResponse(PriceDto dto) {
        PriceResponse response = new PriceResponse();
        response.setId(dto.getId());
        response.setItemId(dto.getItemId());
        response.setStoreId(dto.getStoreId());
        response.setPrice(dto.getPrice());
        return response;
    }
}
