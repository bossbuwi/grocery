package com.paradoxdevs.grocery.controller;

import com.paradoxdevs.grocery.api.request.PriceRequest;
import com.paradoxdevs.grocery.api.response.PriceResponse;
import com.paradoxdevs.grocery.service.PriceService;
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
@RequestMapping("/prices")
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping("/price")
    public ResponseEntity<PriceResponse> create(
            @Valid @RequestBody PriceRequest request
    ) {
        return priceService.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<PriceResponse>> findAll() {
        return ResponseEntity.ok(priceService.findAll());
    }

    @GetMapping("/price/{id}")
    public ResponseEntity<PriceResponse> findById(@PathVariable Long id) {
        return priceService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/price/{id}")
    public ResponseEntity<PriceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PriceRequest request
    ) {
        return priceService.updateById(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/price/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return priceService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
