package com.paradoxdevs.grocery.api.response;

import com.paradoxdevs.grocery.entity.BasketStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BasketResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private BasketStatus status;
    private List<BasketItemResponse> items;
    private BigDecimal total;
}
