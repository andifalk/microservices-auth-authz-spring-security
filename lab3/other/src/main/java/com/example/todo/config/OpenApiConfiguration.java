package com.example.todo.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public GroupedOpenApi toDoApi() {
        return GroupedOpenApi.builder()
                .group("todo").displayName("Suggest ToDo API").pathsToMatch("/api/suggest/**")
                .build();
    }
}
