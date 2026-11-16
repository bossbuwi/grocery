package com.paradoxdevs.grocery.api.response;

import lombok.Data;

@Data
public class ItemResponse {
    private Long id;
    private String name;
    private String barcode;
}
