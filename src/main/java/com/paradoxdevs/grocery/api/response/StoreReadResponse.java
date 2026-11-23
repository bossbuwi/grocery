package com.paradoxdevs.grocery.api.response;

import lombok.Data;

import java.util.List;

@Data
public class StoreReadResponse {
    private Long id;
    private String name;
    private List<StoreItemResponse> items = List.of();
}
