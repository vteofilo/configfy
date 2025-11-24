package com.configfy.configfyapi.repository;

import com.configfy.configfyapi.domain.model.Environment;
import lombok.NonNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EnvironmentRepository extends R2dbcRepository<Environment, UUID> {

    Mono<Environment> findByKey(@NonNull String key);

    Mono<Environment> findByApiKey(@NonNull String apiKey);

    Flux<Environment> findByIsActiveTrueOrderBySortOrderAsc();

    Mono<Boolean> existsByKey(@NonNull String key);

    @Query("UPDATE environments SET usage_count = usage_count + 1, last_used_at = NOW() WHERE id = :id")
    Mono<Void> incrementUsage(UUID id);
}
