package com.example.todo.security;

import com.example.todo.service.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class JwtUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    public JwtUserAuthenticationConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userIdentifier = UUID.fromString(jwt.getSubject());
        return userService.findOneByIdentifier(userIdentifier).map(u ->
            new UsernamePasswordAuthenticationToken(u, jwt.getTokenValue(), u.getAuthorities())
        ).orElse(null);
    }
}
