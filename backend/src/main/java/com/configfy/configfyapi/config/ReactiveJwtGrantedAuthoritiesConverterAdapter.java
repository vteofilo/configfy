package com.configfy.configfyapi.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import reactor.core.publisher.Flux;

import java.util.Collection;

public class ReactiveJwtGrantedAuthoritiesConverterAdapter implements Converter<Jwt, Flux<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter delegate;

    public ReactiveJwtGrantedAuthoritiesConverterAdapter(JwtGrantedAuthoritiesConverter delegate) {
        this.delegate = delegate;
    }

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = delegate.convert(jwt);
        return Flux.fromIterable(authorities);
    }
}
