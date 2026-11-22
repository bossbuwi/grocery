package com.paradoxdevs.grocery.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StoreRequest {
    @NotBlank
    @Pattern(regexp = "\\A\\S(?:[\\s\\S]*\\S)?\\z", message = "must not start or end with whitespace")
    private String name;
}
