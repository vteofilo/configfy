package com.configfy.configfyapi.controller;

import com.configfy.configfyapi.domain.dto.CreateEnvironmentRequest;
import com.configfy.configfyapi.domain.dto.EnvironmentDTO;
import com.configfy.configfyapi.domain.model.Environment;
import com.configfy.configfyapi.exception.*;
import com.configfy.configfyapi.mapper.EnvironmentMapper;
import com.configfy.configfyapi.service.EnvironmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = EnvironmentController.class)
@Import(TestSecurityConfig.class)
class EnvironmentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private EnvironmentService service;

    @MockitoBean
    private EnvironmentMapper mapper;

    private Environment testEnvironment;
    private EnvironmentDTO testEnvironmentDTO;

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

        testEnvironmentDTO = new EnvironmentDTO();
        testEnvironmentDTO.setId(testEnvironment.getId());
        testEnvironmentDTO.setKey(testEnvironment.getKey());
        testEnvironmentDTO.setName(testEnvironment.getName());
        testEnvironmentDTO.setDescription(testEnvironment.getDescription());
        testEnvironmentDTO.setColor(testEnvironment.getColor());
        testEnvironmentDTO.setIcon(testEnvironment.getIcon());
        testEnvironmentDTO.setSortOrder(testEnvironment.getSortOrder());
        testEnvironmentDTO.setIsProtected(testEnvironment.getIsProtected());
        testEnvironmentDTO.setIsActive(testEnvironment.getIsActive());
        testEnvironmentDTO.setUsageCount(testEnvironment.getUsageCount());
        testEnvironmentDTO.setCreatedAt(testEnvironment.getCreatedAt());

        when(mapper.toDTO(any(Environment.class)))
                .thenReturn(testEnvironmentDTO);
        when(mapper.toEntity(any(EnvironmentDTO.class)))
                .thenReturn(testEnvironment);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEnvironments_ShouldReturn200WithList() {
        // Given
        when(service.listActive())
                .thenReturn(Flux.just(testEnvironment));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/environments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnvironmentDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getKey()).isEqualTo("development");
                    assertThat(list.get(0).getName()).isEqualTo("Development");
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByKey_WhenExists_ShouldReturn200() {
        // Given
        when(service.getByKey("development"))
                .thenReturn(Mono.just(testEnvironment));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/environments/development")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnvironmentDTO.class)
                .value(dto -> {
                    assertThat(dto.getKey()).isEqualTo("development");
                    assertThat(dto.getName()).isEqualTo("Development");
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByKey_WhenNotExists_ShouldReturn404() {
        // Given
        when(service.getByKey("non-existent"))
                .thenReturn(Mono.error(new ResourceNotFoundException(
                        ErrorCode.ENVIRONMENT_NOT_FOUND,
                        "Environment",
                        "non-existent"
                )));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/environments/non-existent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create_WhenValid_ShouldReturn201() {
        // Given
        CreateEnvironmentRequest request = new CreateEnvironmentRequest();
        request.setKey("staging");
        request.setName("Staging");
        request.setColor("#f39c12");
        request.setIcon("flask");
        request.setSortOrder(2);

        when(service.create(any(CreateEnvironmentRequest.class), eq("admin")))
                .thenReturn(Mono.just(testEnvironment));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/environments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnvironmentDTO.class)
                .value(dto -> {
                    assertThat(dto.getKey()).isNotNull();
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create_WhenInvalid_ShouldReturn400() {
        // Given
        CreateEnvironmentRequest request = new CreateEnvironmentRequest();
        request.setName("Staging");

        // When & Then
        webTestClient.post()
                .uri("/api/v1/environments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_WhenExists_ShouldReturn204() {
        // Given
        UUID id = testEnvironment.getId();
        when(service.delete(eq(id), eq("admin")))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/environments/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_WhenProtected_ShouldReturn403() {
        // Given
        UUID id = testEnvironment.getId();
        when(service.delete(eq(id), eq("admin")))
                .thenReturn(Mono.error(new EnvironmentProtectedException("production")));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/environments/{id}", id)
                .exchange()
                .expectStatus().isForbidden();
    }
}
