package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.BasketAddItemRequest;
import com.paradoxdevs.grocery.api.request.BasketAddItemsRequest;
import com.paradoxdevs.grocery.api.request.BasketCreateRequest;
import com.paradoxdevs.grocery.api.response.BasketItemResponse;
import com.paradoxdevs.grocery.api.response.BasketResponse;
import com.paradoxdevs.grocery.entity.Basket;
import com.paradoxdevs.grocery.entity.BasketItem;
import com.paradoxdevs.grocery.entity.BasketStatus;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.exception.BasketClosedException;
import com.paradoxdevs.grocery.error.exception.BasketItemNotPricedException;
import com.paradoxdevs.grocery.error.exception.BasketQuantityLimitException;
import com.paradoxdevs.grocery.repository.BasketItemRepository;
import com.paradoxdevs.grocery.repository.BasketRepository;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import com.paradoxdevs.grocery.service.BasketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BasketServiceImpl implements BasketService {
    private final BasketRepository basketRepository;
    private final BasketItemRepository basketItemRepository;
    private final StoreRepository storeRepository;
    private final ItemRepository itemRepository;
    private final PriceRepository priceRepository;

    public BasketServiceImpl(
            BasketRepository basketRepository,
            BasketItemRepository basketItemRepository,
            StoreRepository storeRepository,
            ItemRepository itemRepository,
            PriceRepository priceRepository
    ) {
        this.basketRepository = basketRepository;
        this.basketItemRepository = basketItemRepository;
        this.storeRepository = storeRepository;
        this.itemRepository = itemRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    @Transactional
    public Optional<BasketResponse> create(BasketCreateRequest request) {
        Optional<Store> store = storeRepository.findById(request.getStoreId());
        if (store.isEmpty()) {
            return Optional.empty();
        }

        Basket basket = new Basket();
        basket.setStore(store.get());
        basket.setStatus(BasketStatus.ACTIVE);
        Basket savedBasket = basketRepository.save(basket);
        log.info("Created basket id={} storeId={}", savedBasket.getId(), request.getStoreId());
        return basketRepository.findById(savedBasket.getId()).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BasketResponse> findAll() {
        return basketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BasketResponse> findById(Long id) {
        return basketRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional
    public Optional<BasketResponse> addItem(Long basketId, BasketAddItemRequest request) {
        BasketAddItemsRequest batchRequest = new BasketAddItemsRequest();
        batchRequest.setItems(List.of(request));
        return addItems(basketId, batchRequest);
    }

    @Override
    @Transactional
    public Optional<BasketResponse> addItems(Long basketId, BasketAddItemsRequest request) {
        Optional<Basket> existingBasket = basketRepository.findById(basketId);
        if (existingBasket.isEmpty()) {
            return Optional.empty();
        }

        Basket basket = existingBasket.get();
        requireActive(basket);

        Map<Long, Long> requestedQuantities = new LinkedHashMap<>();
        for (BasketAddItemRequest itemRequest : request.getItems()) {
            long totalQuantity = requestedQuantities.getOrDefault(itemRequest.getItemId(), 0L)
                    + itemRequest.getQuantity();
            if (totalQuantity > Integer.MAX_VALUE) {
                throw new BasketQuantityLimitException();
            }
            requestedQuantities.put(itemRequest.getItemId(), totalQuantity);
        }

        List<Item> requestedItems = itemRepository.findAllById(requestedQuantities.keySet());
        if (requestedItems.size() != requestedQuantities.size()) {
            return Optional.empty();
        }

        Map<Long, Price> pricesByItemId = priceRepository
                .findAllByItemIdInAndStoreId(requestedQuantities.keySet(), basket.getStore().getId())
                .stream()
                .collect(Collectors.toMap(price -> price.getItem().getId(), Function.identity()));
        if (pricesByItemId.size() != requestedQuantities.size()) {
            throw new BasketItemNotPricedException();
        }

        Map<Long, BasketItem> basketItemsByItemId = basket.getItems().stream()
                .collect(Collectors.toMap(
                        basketItem -> basketItem.getPrice().getItem().getId(),
                        Function.identity()
                ));
        for (Map.Entry<Long, Long> entry : requestedQuantities.entrySet()) {
            BasketItem basketItem = basketItemsByItemId.get(entry.getKey());
            int currentQuantity = basketItem == null ? 0 : basketItem.getQuantity();
            if (entry.getValue() > Integer.MAX_VALUE - currentQuantity) {
                throw new BasketQuantityLimitException();
            }
        }

        for (Map.Entry<Long, Long> entry : requestedQuantities.entrySet()) {
            Price price = pricesByItemId.get(entry.getKey());
            BasketItem basketItem = basketItemsByItemId.get(entry.getKey());
            if (basketItem == null) {
                basketItem = new BasketItem();
                basketItem.setBasket(basket);
                basketItem.setPrice(price);
                basket.getItems().add(basketItem);
            }
            basketItem.setQuantity(Math.toIntExact(basketItem.getQuantity() + entry.getValue()));
            basketItemRepository.save(basketItem);
            log.info("Added item to basket id={} itemId={} quantity={}",
                    basketId, entry.getKey(), entry.getValue());
        }
        return Optional.of(toResponse(basket));
    }

    @Override
    @Transactional
    public Optional<BasketResponse> removeItem(Long basketId, Long itemId) {
        Optional<Basket> existingBasket = basketRepository.findById(basketId);
        if (existingBasket.isEmpty()) {
            return Optional.empty();
        }

        Basket basket = existingBasket.get();
        requireActive(basket);

        Optional<BasketItem> basketItem = basketItemRepository
                .findByBasket_IdAndPrice_Item_Id(basketId, itemId);
        if (basketItem.isEmpty()) {
            return Optional.empty();
        }

        basket.getItems().remove(basketItem.get());
        log.info("Removed item from basket id={} itemId={}", basketId, itemId);
        return Optional.of(toResponse(basket));
    }

    @Override
    @Transactional
    public Optional<BasketResponse> close(Long basketId) {
        Optional<Basket> existingBasket = basketRepository.findById(basketId);
        if (existingBasket.isEmpty()) {
            return Optional.empty();
        }

        Basket basket = existingBasket.get();
        if (basket.getStatus() == BasketStatus.ACTIVE) {
            basket.setStatus(BasketStatus.CLOSED);
            basketRepository.save(basket);
            log.info("Closed basket id={}", basketId);
        }
        return Optional.of(toResponse(basket));
    }

    private void requireActive(Basket basket) {
        if (basket.getStatus() == BasketStatus.CLOSED) {
            throw new BasketClosedException();
        }
    }

    private BasketResponse toResponse(Basket basket) {
        BasketResponse response = new BasketResponse();
        response.setId(basket.getId());
        response.setStoreId(basket.getStore().getId());
        response.setStoreName(basket.getStore().getName());
        response.setStatus(basket.getStatus());

        List<BasketItemResponse> items = basket.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        response.setItems(items);
        response.setTotal(items.stream()
                .map(BasketItemResponse::getLineTotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add));
        return response;
    }

    private BasketItemResponse toItemResponse(BasketItem basketItem) {
        BasketItemResponse response = new BasketItemResponse();
        response.setItemId(basketItem.getPrice().getItem().getId());
        response.setItemName(basketItem.getPrice().getItem().getName());
        response.setQuantity(basketItem.getQuantity());
        response.setUnitPrice(basketItem.getPrice().getPrice());
        response.setLineTotal(basketItem.getPrice().getPrice()
                .multiply(BigDecimal.valueOf(basketItem.getQuantity())));
        return response;
    }
}
