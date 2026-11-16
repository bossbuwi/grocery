package com.paradoxdevs.grocery.api.request;

import lombok.Data;

@Data
public class ItemRequest {
    private String name;
    private String barcode;
}
