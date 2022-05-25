package com.example.todo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudienceValidator.class);

    private final OAuth2Error error =
            new OAuth2Error("invalid_token", "The required audience 'http://localhost:9090/api/todos' is missing", null);

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains("http://localhost:9090/api/todos")) {
            LOGGER.info("Successfully validate audience");
            return OAuth2TokenValidatorResult.success();
        } else {
            LOGGER.warn(error.getDescription());
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}