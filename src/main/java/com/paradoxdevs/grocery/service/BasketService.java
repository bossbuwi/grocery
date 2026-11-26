package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.BasketAddItemRequest;
import com.paradoxdevs.grocery.api.request.BasketAddItemsRequest;
import com.paradoxdevs.grocery.api.request.BasketCreateRequest;
import com.paradoxdevs.grocery.api.response.BasketResponse;

import java.util.List;
import java.util.Optional;

public interface BasketService {
    Optional<BasketResponse> create(BasketCreateRequest request);

    List<BasketResponse> findAll();

    Optional<BasketResponse> findById(Long id);

    Optional<BasketResponse> addItem(Long basketId, BasketAddItemRequest request);

    Optional<BasketResponse> addItems(Long basketId, BasketAddItemsRequest request);

    Optional<BasketResponse> removeItem(Long basketId, Long itemId);

    Optional<BasketResponse> close(Long basketId);
}
