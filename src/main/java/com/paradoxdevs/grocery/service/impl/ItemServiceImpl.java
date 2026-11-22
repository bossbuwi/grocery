package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;
import com.paradoxdevs.grocery.dto.ItemDto;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.ItemStorePriceRepository;
import com.paradoxdevs.grocery.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemStorePriceRepository itemStorePriceRepository;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            ItemStorePriceRepository itemStorePriceRepository
    ) {
        this.itemRepository = itemRepository;
        this.itemStorePriceRepository = itemStorePriceRepository;
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
    public Optional<ItemResponse> findById(Long id) {
        return itemRepository.findById(id)
                .map(this::toDto)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            return false;
        }

        itemStorePriceRepository.deleteByItemId(id);
        itemRepository.delete(item.get());
        log.info("Deleted item id={}", id);
        return true;
    }

    @Override
    public List<ItemResponse> findAll() {
        List<ItemDto> itemDtos = itemRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();

        return itemDtos.stream()
                .map(this::toResponse)
                .toList();
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
}
