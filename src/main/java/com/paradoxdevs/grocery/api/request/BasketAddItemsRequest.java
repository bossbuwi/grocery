package com.paradoxdevs.grocery.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BasketAddItemsRequest {
    @NotEmpty
    @Valid
    private List<BasketAddItemRequest> items = new ArrayList<>();
}
