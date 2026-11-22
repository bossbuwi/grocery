package com.paradoxdevs.grocery.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceDto {
    private Long id;
    private Long itemId;
    private Long storeId;
    private BigDecimal price;
}
