package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    ItemResponse create(ItemRequest itemRequest);

    Optional<ItemResponse> findById(Long id);

    boolean deleteById(Long id);

    List<ItemResponse> findAll();
}
