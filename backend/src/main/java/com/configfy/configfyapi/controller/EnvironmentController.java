package com.configfy.configfyapi.controller;

import com.configfy.configfyapi.domain.dto.CreateEnvironmentRequest;
import com.configfy.configfyapi.domain.dto.EnvironmentDTO;
import com.configfy.configfyapi.mapper.EnvironmentMapper;
import com.configfy.configfyapi.security.annotation.*;
import com.configfy.configfyapi.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final EnvironmentMapper mapper;

    @GetMapping
    @Authenticated
    public Flux<EnvironmentDTO> listEnvironments() {
        return environmentService.listActive()
                .map(mapper::toDTO);
    }

    @GetMapping("/{key}")
    @Authenticated
    public Mono<EnvironmentDTO> getByKey(@PathVariable String key) {
        return environmentService.getByKey(key)
                .map(mapper::toDTO);
    }

    @PostMapping
    @AdminOnly
    public Mono<ResponseEntity<EnvironmentDTO>> create(
            @Valid @RequestBody CreateEnvironmentRequest request,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return environmentService.create(request, userId)
                .map(environment -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(mapper.toDTO(environment)));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public Mono<ResponseEntity<Void>> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return environmentService.delete(id, userId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

}
