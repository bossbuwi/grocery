package com.paradoxdevs.grocery.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank
    @Pattern(regexp = "\\A\\S(?:[\\s\\S]*\\S)?\\z", message = "must not start or end with whitespace")
    private String name;

    @Pattern(regexp = "\\A[0-9]{8,13}\\z", message = "must be 8 to 13 digits")
    private String barcode;
}
