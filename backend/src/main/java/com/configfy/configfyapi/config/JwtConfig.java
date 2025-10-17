package com.configfy.configfyapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        OAuth2TokenValidator<Jwt> multiIssuerValidator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new MultiIssuerValidator(Arrays.asList(
                        "http://localhost:8081/realms/configfyrealm",
                        "http://keycloak:8080/realms/configfyrealm"
                ))
        );

        decoder.setJwtValidator(multiIssuerValidator);
        return decoder;
    }

    private static class MultiIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final List<String> validIssuers;

        public MultiIssuerValidator(List<String> validIssuers) {
            this.validIssuers = validIssuers;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            String issuer = jwt.getIssuer().toString();

            if (validIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "The iss claim is not valid. Expected one of: " + validIssuers +
                                    " but got: " + issuer,
                            null
                    )
            );
        }
    }
}
