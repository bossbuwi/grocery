package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceResponse {
    private Long id;
    private Long itemId;
    private Long storeId;
    private BigDecimal price;
}
