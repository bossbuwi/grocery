package com.paradoxdevs.grocery.dto;

import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private String barcode;
}
