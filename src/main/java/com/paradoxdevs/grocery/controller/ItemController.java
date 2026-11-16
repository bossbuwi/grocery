package com.paradoxdevs.grocery.controller;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;
import com.paradoxdevs.grocery.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/item")
    public ResponseEntity<ItemResponse> createItem(@RequestBody ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(itemRequest));
    }

    @GetMapping("/")
    public ResponseEntity<List<ItemResponse>> findAllItems() {
        return ResponseEntity.ok(itemService.findAll());
    }
}
