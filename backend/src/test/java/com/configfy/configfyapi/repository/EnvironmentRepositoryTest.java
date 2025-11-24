package com.configfy.configfyapi.repository;

import com.configfy.configfyapi.domain.model.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private EnvironmentRepository repository;

    @Test
    void testSave_ShouldPersistEnvironment() {
        // Given
        Environment environment = createTestEnvironment("test-env", "Test Environment");

        // When
        Mono<Environment> savedMono = repository.save(environment);

        // Then
        StepVerifier.create(savedMono)
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotNull();
                    assertThat(saved.getKey()).isEqualTo(environment.getKey());
                    assertThat(saved.getName()).isEqualTo("Test Environment");
                    assertThat(saved.getApiKey()).isEqualTo(environment.getApiKey());
                    assertThat(saved.getColor()).isEqualTo("#3498db");
                    assertThat(saved.getIcon()).isEqualTo("flask");
                    assertThat(saved.getSortOrder()).isEqualTo(10);
                    assertThat(saved.getIsProtected()).isFalse();
                    assertThat(saved.getIsActive()).isTrue();
                    assertThat(saved.getUsageCount()).isEqualTo(0L);
                    assertThat(saved.getCreatedBy()).isEqualTo(SYSTEM_USER_ID);
                })
                .verifyComplete();

        // Then
        Mono<Environment> foundMono = repository.findByKey(environment.getKey());

        StepVerifier.create(foundMono)
                .assertNext(found -> {
                    assertThat(found.getKey()).isEqualTo(environment.getKey());
                    assertThat(found.getName()).isEqualTo("Test Environment");
                })
                .verifyComplete();
    }

    @Test
    void testSave_WhenUpdate_ShouldModifyExistingEnvironment() {
        // Given
        Environment environment = createTestEnvironment("test-dev", "Development");
        Environment saved = repository.save(environment).block();

        // When
        saved.setName("Development Updated");
        saved.setDescription("Updated description");
        Mono<Environment> updatedMono = repository.save(saved);

        // Then
        StepVerifier.create(updatedMono)
                .assertNext(updated -> {
                    assertThat(updated.getId()).isEqualTo(saved.getId());
                    assertThat(updated.getName()).isEqualTo("Development Updated");
                    assertThat(updated.getDescription()).isEqualTo("Updated description");
                })
                .verifyComplete();

        StepVerifier.create(repository.findById(saved.getId()))
                .assertNext(found -> {
                    assertThat(found.getName()).isEqualTo("Development Updated");
                })
                .verifyComplete();
    }

    @Test
    void testFindById_WhenExists_ShouldReturnEnvironment() {
        // Given
        Environment saved = repository.save(createTestEnvironment("test-dev", "Development")).block();

        // When
        Mono<Environment> result = repository.findById(saved.getId());

        // Then
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getKey()).isEqualTo(saved.getKey());
                    assertThat(found.getName()).isEqualTo("Development");
                })
                .verifyComplete();
    }

    @Test
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID randomId = UUID.randomUUID();

        // When
        Mono<Environment> result = repository.findById(randomId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindByKey_WhenExists_ShouldReturnEnvironment() {
        // Given
        Environment environment = createTestEnvironment("test-staging", "Staging");
        repository.save(environment).block();

        // When
        Mono<Environment> result = repository.findByKey(environment.getKey());

        // Then
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found.getKey()).isEqualTo(environment.getKey());
                    assertThat(found.getName()).isEqualTo("Staging");
                })
                .verifyComplete();
    }

    @Test
    void testFindByKey_WhenNotExists_ShouldReturnEmpty() {
        // When
        Mono<Environment> result = repository.findByKey("non-existent");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindByKey_IsCaseSensitive() {
        // Given
        repository.save(createTestEnvironment("test-production", "Production")).block();

        // When
        Mono<Environment> result = repository.findByKey("PRODUCTION");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindByApiKey_WhenExists_ShouldReturnEnvironment() {
        // Given
        Environment saved = repository.save(createTestEnvironment("test-dev", "Development")).block();

        // When
        Mono<Environment> result = repository.findByApiKey(saved.getApiKey());

        // Then
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getApiKey()).isEqualTo(saved.getApiKey());
                })
                .verifyComplete();
    }

    @Test
    void testFindByApiKey_WhenNotExists_ShouldReturnEmpty() {
        // When
        Mono<Environment> result = repository.findByApiKey("sdk_invalid_key");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testExistsByKey_WhenExists_ShouldReturnTrue() {
        // Given
        Environment environment = createTestEnvironment("test-dev", "Development");
        repository.save(environment).block();

        // When
        Mono<Boolean> result = repository.existsByKey(environment.getKey());

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testExistsByKey_WhenNotExists_ShouldReturnFalse() {
        // When
        Mono<Boolean> result = repository.existsByKey("non-existent");

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testDelete_ShouldRemoveEnvironment() {
        // Given
        Environment saved = repository.save(createTestEnvironment("to-delete", "To Delete")).block();

        // When
        Mono<Void> deleteMono = repository.delete(saved);

        // Then
        StepVerifier.create(deleteMono)
                .verifyComplete();

        StepVerifier.create(repository.findById(saved.getId()))
                .verifyComplete();
    }

    @Test
    void testDeleteById_ShouldRemoveEnvironment() {
        // Given
        Environment saved = repository.save(createTestEnvironment("to-delete", "To Delete")).block();

        // When
        Mono<Void> deleteMono = repository.deleteById(saved.getId());

        // Then
        StepVerifier.create(deleteMono)
                .verifyComplete();

        StepVerifier.create(repository.existsByKey("to-delete"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testSave_WhenDuplicateKey_ShouldFail() {
        // Given
        Environment env = createTestEnvironment("test-dev", "Development");
        repository.save(env).block();

        // When
        Environment duplicate = Environment.builder()
                .key(env.getKey())
                .name("Development 2")
                .description("Test environment for Development 2")
                .apiKey("sdk_test_" + env.getKey())
                .color("#3498db")
                .icon("flask")
                .sortOrder(10)
                .isProtected(false)
                .isActive(true)
                .usageCount(0L)
                .createdAt(LocalDateTime.now())
                .createdBy(SYSTEM_USER_ID)
                .build();
        Mono<Environment> result = repository.save(duplicate);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void testSave_WhenDuplicateApiKey_ShouldFail() {
        // Given
        Environment first = createTestEnvironment("test-dev", "Development");
        first.setApiKey("sdk_same_key");
        repository.save(first).block();

        // When
        Environment second = createTestEnvironment("test-staging", "Staging");
        second.setApiKey("sdk_same_key");
        Mono<Environment> result = repository.save(second);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void testIncrementUsage_ShouldUpdateCountAndTimestamp() {
        // Given
        Environment saved = repository.save(createTestEnvironment("test-dev", "Development")).block();
        assertThat(saved.getUsageCount()).isEqualTo(0L);
        assertThat(saved.getLastUsedAt()).isNull();

        // When
        Mono<Void> result = repository.incrementUsage(saved.getId());

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(repository.findById(saved.getId()))
                .assertNext(updated -> {
                    assertThat(updated.getUsageCount()).isEqualTo(1L);
                    assertThat(updated.getLastUsedAt()).isNotNull();
                })
                .verifyComplete();
    }

    private Environment createTestEnvironment(String key, String name) {
        return createTestEnvironment(key, name, 10, true);
    }

    private Environment createTestEnvironment(String key, String name, Integer sortOrder, Boolean isActive) {
        String uniqueKey = key + "-" + UUID.randomUUID().toString().substring(0, 8);

        return Environment.builder()
                .key(uniqueKey)
                .name(name)
                .description("Test environment for " + name)
                .apiKey("sdk_test_" + uniqueKey)
                .color("#3498db")
                .icon("flask")
                .sortOrder(sortOrder)
                .isProtected(false)
                .isActive(isActive)
                .usageCount(0L)
                .createdAt(LocalDateTime.now())
                .createdBy(SYSTEM_USER_ID)
                .build();
    }
}
