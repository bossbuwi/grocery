package com.paradoxdevs.grocery.controller;

import com.paradoxdevs.grocery.api.request.BasketAddItemRequest;
import com.paradoxdevs.grocery.api.request.BasketAddItemsRequest;
import com.paradoxdevs.grocery.api.request.BasketCreateRequest;
import com.paradoxdevs.grocery.api.response.BasketResponse;
import com.paradoxdevs.grocery.service.BasketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/baskets")
public class BasketController {
    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<BasketResponse>> findAll() {
        return ResponseEntity.ok(basketService.findAll());
    }

    @GetMapping("/basket/{id}")
    public ResponseEntity<BasketResponse> findById(@PathVariable Long id) {
        return basketService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/basket")
    public ResponseEntity<BasketResponse> create(@Valid @RequestBody BasketCreateRequest request) {
        return basketService.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/basket/{id}/items")
    public ResponseEntity<BasketResponse> addItem(
            @PathVariable Long id,
            @Valid @RequestBody BasketAddItemRequest request
    ) {
        return basketService.addItem(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //  TODO: Check if this works properly
    @PostMapping("/basket/{id}/items/batch")
    public ResponseEntity<BasketResponse> addItems(
            @PathVariable Long id,
            @Valid @RequestBody BasketAddItemsRequest request
    ) {
        return basketService.addItems(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/basket/{id}/items/{itemId}")
    public ResponseEntity<BasketResponse> removeItem(
            @PathVariable Long id,
            @PathVariable Long itemId
    ) {
        return basketService.removeItem(id, itemId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/basket/{id}/close")
    public ResponseEntity<BasketResponse> close(@PathVariable Long id) {
        return basketService.close(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
