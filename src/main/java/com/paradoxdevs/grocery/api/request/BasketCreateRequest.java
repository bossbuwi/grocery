package com.paradoxdevs.grocery.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BasketCreateRequest {
    @NotNull
    @Positive
    private Long storeId;
}
