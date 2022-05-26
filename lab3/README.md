# Lab 3: Calling another Microservice

In the first lab we extended an existing Microservice to an OAuth 2.0 and OpenID Connect 1.0 compliant Resource Server.
Target of this lab is to add another Microservice from the Microservice we extended in lab 1.

As already seen in the architecture introduction, there ia also a further Microservice in the picture that is
called by the already existing Microservice of lab 1.

![Workshop Architecture](../docs/images/demo-architecture.png)

## Learning Targets

In this lab we will add functionality to call another Microservice (resource server) with propagating the JWT.

In this lab you will learn how to:

1. How to call other Microservices using the WebClient.
2. How to authenticate at the other Microservice by propagating the existing JWT.

## Folder Contents

In the folder of lab 2 you find 2 applications:

* __initial__: This is the application we will use as starting point for this lab
* __final__: This application is the completed reference for this lab
* __other_: This application is the other Microservice to be called from the ToDo Microservice with the propagated access token.

## Start the Lab

In this lab we will implement:

* Calling another JWT protected Microservice by propagating the original JWT access token
the ToDo application has got.

Please start this lab with project located in _lab3/initial_.

As you can see the call to the other Microservice (to suggest ToDo's) is already implemented in the _ToDoService_.
What's missing is the corresponding token, so currently the ToDo Microservice would report an 401 http status.

```java
package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Transactional(readOnly = true)
public class ToDoService {

    @Value("${suggest.uri}")
    private String suggestServerUri;
    private final WebClient webClient;

    // ...

    public Mono<SuggestedToDoItem> suggestToDoItem() {
        return webClient
                .get()
                .uri(suggestServerUri + "/api/suggest")
                .retrieve()
                .onStatus(
                        s -> s.equals(HttpStatus.UNAUTHORIZED),
                        cr -> Mono.just(new BadCredentialsException("Not authenticated")))
                .onStatus(
                        HttpStatus::is4xxClientError,
                        cr -> Mono.just(new IllegalArgumentException(cr.statusCode().getReasonPhrase())))
                .onStatus(
                        HttpStatus::is5xxServerError,
                        cr -> Mono.just(new Exception(cr.statusCode().getReasonPhrase())))
                .bodyToMono(SuggestedToDoItem.class);
    }

    // ...
}
```

To enable propagating the already available access token to the other Microservice Spring please extend the bean definition of the _WebClient_
with the servlet filter _ServletBearerExchangeFilterFunction_. This filter gets the existing access token 

```
package com.example.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(new ServletBearerExchangeFilterFunction())
                .build();
    }

}
```

Please also have a look at the other tests as well in the reference solution.

<hr>

This is the end of this lab and of the workshop.
