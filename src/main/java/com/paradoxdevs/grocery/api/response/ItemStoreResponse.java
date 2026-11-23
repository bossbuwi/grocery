package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemStoreResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}
