package com.example.todo;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@SecuritySchemes({
        @SecurityScheme(
                name = "bearer",
                type = HTTP,
                scheme = "bearer"
        )}
)
@SpringBootApplication
public class SuggestToDoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuggestToDoApplication.class, args);
    }

}
