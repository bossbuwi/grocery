package com.paradoxdevs.grocery.controller;

import com.paradoxdevs.grocery.api.request.StoreRequest;
import com.paradoxdevs.grocery.api.response.StoreResponse;
import com.paradoxdevs.grocery.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/store")
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest storeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.create(storeRequest));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<StoreResponse>> findAllStores() {
        return ResponseEntity.ok(storeService.findAll());
    }

    @GetMapping("/store/{id}")
    public ResponseEntity<StoreResponse> findStoreById(@PathVariable Long id) {
        return storeService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/store/{id}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest storeRequest
    ) {
        return storeService.updateById(id, storeRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/store/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        return storeService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
