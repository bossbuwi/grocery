package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemReadResponse;
import com.paradoxdevs.grocery.api.response.ItemStoreResponse;
import com.paradoxdevs.grocery.api.response.ItemResponse;
import com.paradoxdevs.grocery.dto.ItemDto;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final PriceRepository priceRepository;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            PriceRepository priceRepository
    ) {
        this.itemRepository = itemRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    public ItemResponse create(ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setBarcode(itemRequest.getBarcode());

        Item savedItem = itemRepository.save(item);
        log.info("Created item id={}", savedItem.getId());
        ItemDto itemDto = toDto(savedItem);
        return toResponse(itemDto);
    }

    @Override
    public Optional<ItemResponse> updateById(Long id, ItemRequest itemRequest) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(itemRequest.getName());
                    item.setBarcode(itemRequest.getBarcode());
                    Item updatedItem = itemRepository.save(item);
                    log.info("Updated item id={}", updatedItem.getId());
                    return updatedItem;
                })
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    public Optional<ItemReadResponse> findById(Long id) {
        return itemRepository.findById(id)
                .map(item -> toReadResponses(List.of(item)).getFirst());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            return false;
        }

        priceRepository.deleteByItemId(id);
        itemRepository.delete(item.get());
        log.info("Deleted item id={}", id);
        return true;
    }

    @Override
    public List<ItemReadResponse> findAll() {
        return toReadResponses(itemRepository.findAll());
    }

    private ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setBarcode(item.getBarcode());
        return itemDto;
    }

    private ItemResponse toResponse(ItemDto itemDto) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setId(itemDto.getId());
        itemResponse.setName(itemDto.getName());
        itemResponse.setBarcode(itemDto.getBarcode());
        return itemResponse;
    }

    private List<ItemReadResponse> toReadResponses(List<Item> items) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        Map<Long, List<ItemStoreResponse>> storesByItemId = priceRepository
                .findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        price -> price.getItem().getId(),
                        Collectors.mapping(this::toItemStoreResponse, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    ItemReadResponse response = new ItemReadResponse();
                    response.setId(item.getId());
                    response.setName(item.getName());
                    response.setBarcode(item.getBarcode());
                    response.setStores(storesByItemId.getOrDefault(item.getId(), List.of()));
                    return response;
                })
                .toList();
    }

    private ItemStoreResponse toItemStoreResponse(Price price) {
        ItemStoreResponse response = new ItemStoreResponse();
        response.setId(price.getStore().getId());
        response.setName(price.getStore().getName());
        response.setPrice(price.getPrice());
        return response;
    }
}
