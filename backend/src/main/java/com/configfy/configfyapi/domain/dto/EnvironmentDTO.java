package com.configfy.configfyapi.domain.dto;

import com.configfy.configfyapi.domain.model.Environment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDTO {
    private UUID id;
    private String key;
    private String name;
    private String description;
    private String apiKey;
    private String color;
    private String icon;
    private Integer sortOrder;
    private Boolean isProtected;
    private Boolean isActive;
    private Long usageCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}
