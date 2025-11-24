package com.configfy.configfyapi.service;

import com.configfy.configfyapi.domain.dto.CreateEnvironmentRequest;
import com.configfy.configfyapi.domain.model.Environment;
import com.configfy.configfyapi.exception.DuplicateResourceException;
import com.configfy.configfyapi.exception.EnvironmentProtectedException;
import com.configfy.configfyapi.exception.InvalidApiKeyException;
import com.configfy.configfyapi.exception.ResourceNotFoundException;
import com.configfy.configfyapi.repository.EnvironmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvironmentServiceTest {

    @Mock
    private EnvironmentRepository repository;

    @InjectMocks
    private EnvironmentService service;

    private Environment testEnvironment;
    private CreateEnvironmentRequest createRequest;

    @BeforeEach
    void setUp() {
        testEnvironment = Environment.builder()
                .id(UUID.randomUUID())
                .key("development")
                .name("Development")
                .description("Dev environment")
                .apiKey("sdk_dev_test123")
                .color("#3498db")
                .icon("code")
                .sortOrder(1)
                .isProtected(false)
                .isActive(true)
                .usageCount(0L)
                .createdAt(LocalDateTime.now())
                .createdBy("user-123")
                .build();

        createRequest = new CreateEnvironmentRequest();
        createRequest.setKey("staging");
        createRequest.setName("Staging");
        createRequest.setDescription("Staging environment");
        createRequest.setColor("#f39c12");
        createRequest.setIcon("flask");
        createRequest.setSortOrder(2);
    }

    @Test
    void listActive_ShouldReturnActiveEnvironments() {
        // Given
        when(repository.findByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Flux.just(testEnvironment));

        // When
        Flux<Environment> result = service.listActive();

        // Then
        StepVerifier.create(result)
                .expectNext(testEnvironment)
                .verifyComplete();

        verify(repository).findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Test
    void listActive_WhenNoActiveEnvironments_ShouldReturnEmpty() {
        // Given
        when(repository.findByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Flux.empty());

        // When
        Flux<Environment> result = service.listActive();

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getByKey_WhenExists_ShouldReturnEnvironment() {
        // Given
        when(repository.findByKey("development"))
                .thenReturn(Mono.just(testEnvironment));

        // When
        Mono<Environment> result = service.getByKey("development");

        // Then
        StepVerifier.create(result)
                .expectNext(testEnvironment)
                .verifyComplete();

        verify(repository).findByKey("development");
    }

    @Test
    void getByKey_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Given
        when(repository.findByKey("non-existent"))
                .thenReturn(Mono.empty());

        // When
        Mono<Environment> result = service.getByKey("non-existent");

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getById_WhenExists_ShouldReturnEnvironment() {
        // Given
        UUID id = testEnvironment.getId();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));

        // When
        Mono<Environment> result = service.getById(id);

        // Then
        StepVerifier.create(result)
                .expectNext(testEnvironment)
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Mono.empty());

        // When
        Mono<Environment> result = service.getById(id);

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void validateApiKey_WhenValid_ShouldReturnEnvironment() {
        // Given
        when(repository.findByApiKey("sdk_dev_test123"))
                .thenReturn(Mono.just(testEnvironment));
        when(repository.incrementUsage(testEnvironment.getId()))
                .thenReturn(Mono.empty());

        // When
        Mono<Environment> result = service.validateApiKey("sdk_dev_test123");

        // Then
        StepVerifier.create(result)
                .expectNext(testEnvironment)
                .verifyComplete();

        verify(repository).findByApiKey("sdk_dev_test123");
        verify(repository).incrementUsage(testEnvironment.getId());
    }

    @Test
    void validateApiKey_WhenInvalid_ShouldThrowInvalidApiKeyException() {
        // Given
        when(repository.findByApiKey("invalid-key"))
                .thenReturn(Mono.empty());

        // When
        Mono<Environment> result = service.validateApiKey("invalid-key");

        // Then
        StepVerifier.create(result)
                .expectError(InvalidApiKeyException.class)
                .verify();
    }

    @Test
    void validateApiKey_WhenInactive_ShouldThrowInvalidApiKeyException() {
        // Given
        testEnvironment.setIsActive(false);
        when(repository.findByApiKey("sdk_dev_test123"))
                .thenReturn(Mono.just(testEnvironment));

        // When
        Mono<Environment> result = service.validateApiKey("sdk_dev_test123");

        // Then
        StepVerifier.create(result)
                .expectError(InvalidApiKeyException.class)
                .verify();

        verify(repository, never()).incrementUsage(any());
    }

    @Test
    void create_WhenKeyUnique_ShouldCreateEnvironment() {
        // Given
        when(repository.existsByKey("staging"))
                .thenReturn(Mono.just(false));
        when(repository.save(any(Environment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<Environment> result = service.create(createRequest, "user-123");

        // Then
        StepVerifier.create(result)
                .assertNext(env -> {
                    assertThat(env.getKey()).isEqualTo("staging");
                    assertThat(env.getName()).isEqualTo("Staging");
                    assertThat(env.getApiKey()).startsWith("sdk_stag_");
                    assertThat(env.getIsProtected()).isFalse();
                    assertThat(env.getIsActive()).isTrue();
                    assertThat(env.getCreatedBy()).isEqualTo("user-123");
                })
                .verifyComplete();

        verify(repository).existsByKey("staging");
        verify(repository).save(any(Environment.class));
    }

    @Test
    void create_WhenKeyDuplicate_ShouldThrowDuplicateResourceException() {
        // Given
        when(repository.existsByKey("staging"))
                .thenReturn(Mono.just(true));

        // When
        Mono<Environment> result = service.create(createRequest, "user-123");

        // Then
        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(repository).existsByKey("staging");
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenExists_ShouldUpdateEnvironment() {
        // Given
        UUID id = testEnvironment.getId();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));
        when(repository.save(any(Environment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        CreateEnvironmentRequest updateRequest = new CreateEnvironmentRequest();
        updateRequest.setKey("development");
        updateRequest.setName("Development Updated");
        updateRequest.setDescription("Updated desc");
        updateRequest.setColor("#000000");
        updateRequest.setIcon("updated-icon");
        updateRequest.setSortOrder(5);

        // When
        Mono<Environment> result = service.update(id, updateRequest, "user-123");

        // Then
        StepVerifier.create(result)
                .assertNext(env -> {
                    assertThat(env.getName()).isEqualTo("Development Updated");
                    assertThat(env.getDescription()).isEqualTo("Updated desc");
                    assertThat(env.getColor()).isEqualTo("#000000");
                    assertThat(env.getSortOrder()).isEqualTo(5);
                })
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).save(any(Environment.class));
    }

    @Test
    void update_WhenProtected_ShouldThrowEnvironmentProtectedException() {
        // Given
        testEnvironment.setIsProtected(true);
        UUID id = testEnvironment.getId();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));

        CreateEnvironmentRequest updateRequest = new CreateEnvironmentRequest();
        updateRequest.setKey("different-key");
        updateRequest.setName("New Name");

        // When
        Mono<Environment> result = service.update(id, updateRequest, "user-123");

        // Then
        StepVerifier.create(result)
                .expectError(EnvironmentProtectedException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void regenerateApiKey_WhenExists_ShouldGenerateNewKey() {
        // Given
        UUID id = testEnvironment.getId();
        String oldApiKey = testEnvironment.getApiKey();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));
        when(repository.save(any(Environment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<Environment> result = service.regenerateApiKey(id, "user-123");

        // Then
        StepVerifier.create(result)
                .assertNext(env -> {
                    assertThat(env.getApiKey()).isNotEqualTo(oldApiKey);
                    assertThat(env.getApiKey()).startsWith("sdk_deve_");
                })
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).save(any(Environment.class));
    }

    @Test
    void regenerateApiKey_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Mono.empty());

        // When
        Mono<Environment> result = service.regenerateApiKey(id, "user-123");

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void delete_WhenNotProtected_ShouldDeleteEnvironment() {
        // Given
        UUID id = testEnvironment.getId();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));
        when(repository.delete(testEnvironment))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = service.delete(id, "user-123");

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).delete(testEnvironment);
    }

    @Test
    void delete_WhenProtected_ShouldThrowEnvironmentProtectedException() {
        // Given
        testEnvironment.setIsProtected(true);
        UUID id = testEnvironment.getId();
        when(repository.findById(id))
                .thenReturn(Mono.just(testEnvironment));

        // When
        Mono<Void> result = service.delete(id, "user-123");

        // Then
        StepVerifier.create(result)
                .expectError(EnvironmentProtectedException.class)
                .verify();

        verify(repository, never()).delete(any());
    }

    @Test
    void delete_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = service.delete(id, "user-123");

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
