package com.configfy.configfyapi.domain.model;

import com.configfy.configfyapi.domain.enums.FlagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feature_flags")
public class FeatureFlag {
    @Id
    private UUID id;
    private String key;
    private String name;
    private String description;
    private FlagType type;
    private Boolean enabled;
    private UUID environmentId;
    @Transient
    private Environment environment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
