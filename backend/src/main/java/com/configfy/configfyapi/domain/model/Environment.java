package com.configfy.configfyapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("environments")
public class Environment {
    @Id
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
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime createdAt;
    private String createdBy;
}
