package com.configfy.configfyapi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class ConfigController {

    @GetMapping("/configs")
    public Mono<String> getAllConfigs(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return Mono.just("Configs for user: "+ username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/configs")
    public Mono<String> createConfig(@RequestBody String config) {
        return Mono.just("Config created: " + config);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/configs/{id}")
    public Mono<String> getConfig(@PathVariable String id) {
        return Mono.just("Config: " + id);
    }

}
