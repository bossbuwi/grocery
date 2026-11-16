package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;

import java.util.List;

public interface ItemService {
    ItemResponse create(ItemRequest itemRequest);

    List<ItemResponse> findAll();
}
