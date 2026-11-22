package com.paradoxdevs.grocery.service;

import com.paradoxdevs.grocery.api.request.PriceRequest;
import com.paradoxdevs.grocery.api.response.PriceResponse;

import java.util.List;
import java.util.Optional;

public interface PriceService {
    Optional<PriceResponse> create(PriceRequest request);

    Optional<PriceResponse> updateById(Long id, PriceRequest request);

    Optional<PriceResponse> findById(Long id);

    boolean deleteById(Long id);

    List<PriceResponse> findAll();
}
