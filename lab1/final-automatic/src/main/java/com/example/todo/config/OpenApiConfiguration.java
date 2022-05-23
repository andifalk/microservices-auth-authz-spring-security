package com.example.todo.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public GroupedOpenApi toDoApi() {
        return GroupedOpenApi.builder()
                .group("todo").displayName("ToDo API").pathsToMatch("/api/todos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user").displayName("User API").pathsToMatch("/api/users/**")
                .build();
    }

}
