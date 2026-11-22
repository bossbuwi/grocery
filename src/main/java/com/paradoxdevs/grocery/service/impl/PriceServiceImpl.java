package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.PriceRequest;
import com.paradoxdevs.grocery.api.response.PriceResponse;
import com.paradoxdevs.grocery.dto.PriceDto;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.ItemStorePrice;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.exception.ResourceAlreadyExistsException;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.ItemStorePriceRepository;
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
    private final ItemStorePriceRepository itemStorePriceRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public PriceServiceImpl(
            ItemStorePriceRepository itemStorePriceRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository
    ) {
        this.itemStorePriceRepository = itemStorePriceRepository;
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
        if (itemStorePriceRepository.existsByItemIdAndStoreId(request.getItemId(), request.getStoreId())) {
            throw duplicatePrice(request);
        }

        ItemStorePrice itemStorePrice = new ItemStorePrice();
        itemStorePrice.setItem(item.get());
        itemStorePrice.setStore(store.get());
        itemStorePrice.setPrice(request.getPrice());

        ItemStorePrice savedPrice = itemStorePriceRepository.save(itemStorePrice);
        log.info("Created price id={} itemId={} storeId={}",
                savedPrice.getId(), request.getItemId(), request.getStoreId());
        return Optional.of(toResponse(toDto(savedPrice)));
    }

    @Override
    @Transactional
    public Optional<PriceResponse> updateById(Long id, PriceRequest request) {
        Optional<ItemStorePrice> existingPrice = itemStorePriceRepository.findById(id);
        Optional<Item> item = itemRepository.findById(request.getItemId());
        Optional<Store> store = storeRepository.findById(request.getStoreId());
        if (existingPrice.isEmpty() || item.isEmpty() || store.isEmpty()) {
            return Optional.empty();
        }
        if (itemStorePriceRepository.existsByItemIdAndStoreIdAndIdNot(
                request.getItemId(), request.getStoreId(), id
        )) {
            throw duplicatePrice(request);
        }

        ItemStorePrice price = existingPrice.get();
        price.setItem(item.get());
        price.setStore(store.get());
        price.setPrice(request.getPrice());
        ItemStorePrice savedPrice = itemStorePriceRepository.save(price);
        log.info("Updated price id={} itemId={} storeId={}",
                savedPrice.getId(), request.getItemId(), request.getStoreId());
        return Optional.of(toResponse(toDto(savedPrice)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PriceResponse> findById(Long id) {
        return itemStorePriceRepository.findById(id)
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Optional<ItemStorePrice> price = itemStorePriceRepository.findById(id);
        if (price.isEmpty()) {
            return false;
        }

        itemStorePriceRepository.delete(price.get());
        log.info("Deleted price id={}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> findAll() {
        return itemStorePriceRepository.findAll()
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

    private PriceDto toDto(ItemStorePrice itemStorePrice) {
        PriceDto dto = new PriceDto();
        dto.setId(itemStorePrice.getId());
        dto.setItemId(itemStorePrice.getItem().getId());
        dto.setStoreId(itemStorePrice.getStore().getId());
        dto.setPrice(itemStorePrice.getPrice());
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
