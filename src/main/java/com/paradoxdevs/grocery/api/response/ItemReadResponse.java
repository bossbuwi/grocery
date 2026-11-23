package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ItemReadResponse {
    private Long id;
    private String name;
    private String barcode;
    private List<ItemStoreResponse> stores = List.of();
}
