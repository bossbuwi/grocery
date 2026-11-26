package com.paradoxdevs.grocery.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BasketAddItemRequest {
    @NotNull
    @Positive
    private Long itemId;

    @NotNull
    @Positive
    private Integer quantity = 1;
}
