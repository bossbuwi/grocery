package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;
import com.paradoxdevs.grocery.dto.ItemDto;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemResponse create(ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setBarcode(itemRequest.getBarcode());

        Item savedItem = itemRepository.save(item);
        ItemDto itemDto = toDto(savedItem);
        return toResponse(itemDto);
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
