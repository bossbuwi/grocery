package com.paradoxdevs.grocery.api.request;

import com.paradoxdevs.grocery.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^\\S(?:.*\\S)?$", message = "must not start or end with whitespace")
    private String username;

    @NotBlank
    @Size(min = 8)
    @MaxUtf8Bytes(72)
    private String password;
}
