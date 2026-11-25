package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.response.DashboardSummaryResponse;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public DashboardServiceImpl(ItemRepository itemRepository, StoreRepository storeRepository) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                itemRepository.count(),
                storeRepository.count()
        );
    }
}
