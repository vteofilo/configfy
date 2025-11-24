package com.configfy.configfyapi.service;

import com.configfy.configfyapi.domain.dto.CreateEnvironmentRequest;
import com.configfy.configfyapi.domain.model.Environment;
import com.configfy.configfyapi.exception.*;
import com.configfy.configfyapi.repository.EnvironmentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ENVIRONMENT_RESOURCE_ERROR_CODE = "Environment";

    public Flux<Environment> listActive() {
        return environmentRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public Mono<Environment> getByKey(@NonNull String key) {
        return environmentRepository.findByKey(key)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                ErrorCode.ENVIRONMENT_NOT_FOUND,
                                ENVIRONMENT_RESOURCE_ERROR_CODE,
                                key
                        )
                ));
    }

    public Mono<Environment> getById(UUID id) {
        return environmentRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                ErrorCode.ENVIRONMENT_NOT_FOUND,
                                ENVIRONMENT_RESOURCE_ERROR_CODE,
                                id.toString()
                        )
                ));
    }

    public Mono<Environment> validateApiKey(String apiKey) {
        return environmentRepository.findByApiKey(apiKey)
                .switchIfEmpty(Mono.error(new InvalidApiKeyException("API key not found")))
                .flatMap( env -> {
                    if (!env.getIsActive()) {
                        return Mono.error(new InvalidApiKeyException("Environment is inactive"));
                    }

                    environmentRepository.incrementUsage(env.getId())
                            .doOnError(e -> log.warn("Failed to increment usage", e));

                    return Mono.just(env);
                });
    }

    public Mono<Environment> create(@NonNull CreateEnvironmentRequest request, String userId) {
        return environmentRepository.existsByKey(request.getKey())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                ErrorCode.DUPLICATE_ENVIRONMENT,
                                ENVIRONMENT_RESOURCE_ERROR_CODE,
                                request.getKey()
                        ));
                    }

                    String apiKey = generateApiKey(request.getKey());

                    Environment env = Environment.builder()
                            .key(request.getKey())
                            .name(request.getName())
                            .description(request.getDescription())
                            .apiKey(apiKey)
                            .color(request.getColor())
                            .icon(request.getIcon())
                            .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                            .isProtected(false)
                            .isActive(true)
                            .usageCount(0L)
                            .createdAt(LocalDateTime.now())
                            .createdBy(userId)
                            .build();

                    return environmentRepository.save(env);
                });
    }

    public Mono<Environment> update(UUID id, CreateEnvironmentRequest request, String userId) {
        return environmentRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(ErrorCode.ENVIRONMENT_NOT_FOUND, "Environment", id.toString())
                ))
                .flatMap(env -> {
                    if (env.getIsProtected() && !env.getKey().equals(request.getKey())) {
                        return Mono.error(new EnvironmentProtectedException(env.getKey()));
                    }

                    env.setName(request.getName());
                    env.setDescription(request.getDescription());
                    env.setColor(request.getColor());
                    env.setIcon(request.getIcon());
                    env.setSortOrder(request.getSortOrder());

                    return environmentRepository.save(env);
                });
    }

    public Mono<Environment> regenerateApiKey(UUID environmentId, String userId) {
        return environmentRepository.findById(environmentId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(ErrorCode.ENVIRONMENT_NOT_FOUND, "Environment", environmentId.toString())
                ))
                .flatMap(env -> {
                    String newApiKey = generateApiKey(env.getKey());
                    env.setApiKey(newApiKey);
                    return environmentRepository.save(env);
                });
    }

    public Mono<Void> delete(UUID id, String userId) {
        return environmentRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(ErrorCode.ENVIRONMENT_NOT_FOUND, "Environment", id.toString())
                ))
                .flatMap(env -> {
                    if (env.getIsProtected()) {
                        return Mono.error(new EnvironmentProtectedException(env.getKey()));
                    }
                    return environmentRepository.delete(env);
                });
    }

    private String generateApiKey(String environmentKey) {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String prefix = Optional.ofNullable(environmentKey)
                .filter(key -> !key.isEmpty())
                .map(key -> key.length() >= 4 ? key.substring(0, 4) : key)
                .orElse("env");

        return String.format("sdk_%s_%s", prefix, randomPart);
    }

}
