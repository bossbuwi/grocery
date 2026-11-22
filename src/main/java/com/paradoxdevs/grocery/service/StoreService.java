package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.StoreRequest;
import com.paradoxdevs.grocery.api.response.StoreResponse;

import java.util.List;
import java.util.Optional;

public interface StoreService {
    StoreResponse create(StoreRequest storeRequest);

    Optional<StoreResponse> updateById(Long id, StoreRequest storeRequest);

    Optional<StoreResponse> findById(Long id);

    boolean deleteById(Long id);

    List<StoreResponse> findAll();
}
