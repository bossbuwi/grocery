package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BasketItemResponse {
    private Long itemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
