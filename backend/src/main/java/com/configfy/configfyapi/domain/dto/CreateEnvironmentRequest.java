package com.configfy.configfyapi.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEnvironmentRequest {
    @NotBlank(message = "Key is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Key must contain only lowercase letters, numbers and hyphens")
    @Size(min = 2, max = 50, message = "Key must be between 2 and 50 characters")
    private String key;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color (e.g., #FF5733)")
    private String color;

    private String icon;

    private Integer sortOrder;
}
