package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreItemResponse {
    private Long id;
    private String name;
    private String barcode;
    private BigDecimal price;
}
