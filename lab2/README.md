# Lab 2: Testing an OAuth 2.0/OIDC compliant Resource Server

In the first lab we extended an existing Microservice to an OAuth 2.0 and OpenID Connect 1.0 compliant Resource Server.
Target of this lab is to add automated security tests for this Microservice.

Testing is an important topic. The DevOps culture also propagates _Automate All The Things_.
This applies to writing and automating tests as well.

The important point here is to write the right tests.
A well-known approach is shown as part of the Test-Pyramid by Mike Cohn.

![Test Pyramid](images/test-pyramid.png)

Most tests should be written as easy unit tests, this type of testing is quite cheap and provides fast feedback if things
are still working as expected or anything has been broken.

Integration tests (aka tests on the service layer) are a bit more effort, often these tests depend on a runtime environment
like a Java EE or Spring container. Typically, these tests run significantly slower and are often causing long CI/CD waiting times.

The tests with most effort are acceptance tests, UI tests or End2End tests which do a complete test of the application 
from api to data access layer. These tests run very long and are expensive to write and to maintain.

If you want to get more into this topic then check out this very good article 
for [The Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html).

In this lab we will write tests on the first layer (a unit test) and on the second layer (a security integration test).

## Learning Targets

In this lab we will add security tests for an OAuth2/OIDC compliant resource server.
Tests should have a minimum of dependencies on other components so the tests run without the requirement of a real identity provider.

In this lab you will learn how to:

1. How to write automated tests simulating a bearer token authentication using JSON web tokens (JWT)
2. How to write automated tests to verify authorization based on JWT.

## Folder Contents

In the folder of lab 2 you find 2 applications:

* __initial__: This is the application we will use as starting point for this lab
* __final__: This application is the completed reference for this lab 

## Start the Lab

In this lab we will implement:

* An integration test to verify correct authentication & authorization for the ToDo API using JWT

Please start this lab with project located in _lab2/initial_.

## Integration Test

Open the existing class _com.example.todo.api.ToDoRestControllerIntegrationTest,_ and we will add/change the missing parts.

```java
package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.entity.ToDoItemEntity;
import com.example.todo.entity.ToDoItemEntityRepository;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToDoRestControllerIntegrationTest {

    private static final UUID TODO_ITEM_IDENTIFIER = UUID.randomUUID();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ToDoItemEntityRepository toDoItemEntityRepository;

    @SuppressWarnings("unused")
    @MockBean
    private H2ConsoleProperties h2ConsoleProperties;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                //.apply(springSecurity()) (1)
                .build();
    }

    @Disabled("Not running successfully without authentication") // (2)
    @DisplayName("Retrieving ToDo items for users with different roles")
    @Nested
    class FindAllToDos {

        @DisplayName("returns all todo items for Admin")
        @Test
        void findAllToDosForAdmin() throws Exception {
            ToDoItemEntity toDoItem1 = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            ToDoItemEntity toDoItem2 = getToDoItemEntity(UUID.fromString(DataInitializer.KENT_ID));
            when(toDoItemEntityRepository.findAllByUserIdentifier(any())).thenReturn(List.of(toDoItem1));
            when(toDoItemEntityRepository.findAll()).thenReturn(List.of(toDoItem1, toDoItem2));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.PARKER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
            verify(toDoItemEntityRepository).findAll();
            verify(toDoItemEntityRepository, never()).findAllByUserIdentifier(any());
        }

        @DisplayName("returns only items for user")
        @Test
        void findAllToDosForStandardUser() throws Exception {
            ToDoItemEntity toDoItem1 = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            ToDoItemEntity toDoItem2 = getToDoItemEntity(UUID.fromString(DataInitializer.KENT_ID));
            when(toDoItemEntityRepository.findAllByUserIdentifier(any())).thenReturn(List.of(toDoItem1));
            when(toDoItemEntityRepository.findAll()).thenReturn(List.of(toDoItem1, toDoItem2));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
            verify(toDoItemEntityRepository, never()).findAll();
            verify(toDoItemEntityRepository).findAllByUserIdentifier(any());
        }
    }

    @Disabled("Not running successfully without authentication")
    @DisplayName("Retrieving all ToDos for a user")
    @Nested
    class FindAllToDosForUser {
        @DisplayName("is successful")
        @Test
        void findAllToDosForUser() throws Exception {
            User user = getBruceWayne();
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.findAllByUserIdentifier(any())).thenReturn(List.of(toDoItem));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "[{\"identifier\":\"" + toDoItem.getIdentifier() + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}]"
                    ));
        }

        @DisplayName("fails when getting items for another user")
        @Test
        void findAllToDosForAnotherUser() throws Exception {
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.KENT_ID));
            when(toDoItemEntityRepository.findAllByUserIdentifier(any())).thenReturn(List.of(toDoItem));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.KENT_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @DisplayName("fails when unauthenticated")
        @Test
        void findAllForUserUnauthorized() throws Exception {
            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("fails when unauthorized")
        @Test
        void findAllToDosForUserForbidden() throws Exception {
            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Disabled("Not running successfully without authentication")
    @DisplayName("Retrieving one ToDo for a user")
    @Nested
    class FindOneToDoForUser {
        @DisplayName("is successful")
        @Test
        void findOneToDoForUser() throws Exception {
            User user = getBruceWayne();
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.findOneByIdentifierAndUserIdentifier(any(), any()))
                    .thenReturn(Optional.of(toDoItem));
            mvc.perform(
                            get("/api/todos/{todoIdentifier}", toDoItem.getIdentifier().toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "{\"identifier\":\"" + toDoItem.getIdentifier() + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                    ));
        }

        @DisplayName("fails for getting item of another user")
        @Test
        void findOneToDoForAnotherUser() throws Exception {
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.KENT_ID));
            when(toDoItemEntityRepository.findOneByIdentifierAndUserIdentifier(any(), any()))
                    .thenReturn(Optional.empty());
            mvc.perform(
                            get("/api/todos/{todoIdentifier}", toDoItem.getIdentifier().toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @DisplayName("fails when unauthenticated")
        @Test
        void findOneToDoForUserUnauthorized() throws Exception {
            mvc.perform(
                            get("/api/todos/{todoIdentifier}", UUID.randomUUID().toString()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("fails when unauthorized")
        @Test
        void findOneToDoForUserForbidden() throws Exception {
            mvc.perform(
                            get("/api/todos/{todoIdentifier}", UUID.randomUUID().toString()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

    }

    @Disabled("Not running successfully without authentication")
    @DisplayName("Creating a new ToDo")
    @Nested
    class CreateToDo {
        @DisplayName("is successful")
        @Test
        void createToDoItem() throws Exception {
            User user = getBruceWayne();
            ToDoItem toDoItem = getToDoItem(UUID.fromString(DataInitializer.WAYNE_ID));
            ToDoItemEntity toDoItemEntity = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.save(any(ToDoItemEntity.class)))
                    .thenReturn(toDoItemEntity);
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(toDoItem)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "{\"identifier\":\"" + toDoItem.getIdentifier() + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                    ));
        }

        @DisplayName("fails when unauthenticated")
        @Test
        void createToDoItemUnauthorized() throws Exception {
            ToDoItem toDoItem = getToDoItem(UUID.fromString(DataInitializer.WAYNE_ID));
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(toDoItem)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("fails when unauthorized")
        @Test
        void createToDoItemForbidden() throws Exception {
            ToDoItem toDoItem = getToDoItem(UUID.fromString(DataInitializer.WAYNE_ID));
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(toDoItem)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    private User getBruceWayne() {
        return new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", Set.of("USER"));
    }

    private ToDoItem getToDoItem(UUID userIdentifier) {
        return new ToDoItem(TODO_ITEM_IDENTIFIER, "mytodo",
                "todo description", null, userIdentifier);
    }

    private ToDoItemEntity getToDoItemEntity(UUID userIdentifier) {
        ToDoItemEntity toDoItemEntity = new ToDoItemEntity(TODO_ITEM_IDENTIFIER, "mytodo",
                "todo description", null, userIdentifier);
        ReflectionTestUtils.setField(toDoItemEntity, "id", 1L);
        return toDoItemEntity;
    }

    private Jwt getJwt(String userIdentifier, List<String> roles) { // (3)
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userIdentifier)
                .claim("roles", roles)
                .build();
    }

    static class JwtAuthzConverter implements Converter<Jwt, Collection<GrantedAuthority>> { // (4)
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            return jwt.getClaimAsStringList("roles")
                    .stream()
                    .map(r -> "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }

}
```

First you need to enable support for spring security and uncomment the line with _.apply(springSecurity())_ in (1).
Now security is enabled and all tests calling the secured endpoints will fail with a _401_ status.

All tests are currently disabled as this would break the automatic build of the github action. So please remove all lines 
with the _@Disabled_ annotation same as in line (2).

To add the missing authentication to the tests you have two options:

You can use the provided _getJwt()_ operation and the _JwtAuthzConverter()_ (see markers (3) and (4) above) and add the JWT to the call like here:

```java
// ...
Jwt jwt = getJwt(DataInitializer.PARKER_ID, List.of("USER", "ADMIN"));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.PARKER_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
// ...
```                                    

Or you can make it by more directly specifying the JWT using a lambda expression like here:

```java
// ...
mvc.perform(
    get("/api/todos/{todoIdentifier}", toDoItem.getIdentifier().toString())
            .with(jwt()
                    .jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                    .claim("roles", List.of("USER"))).authorities(new JwtAuthzConverter())))
// ...                    
```


Please do not forget to also test security and especially authorization on the method layer as well (verify the operations annotated with_@PreAuthorize_). As this is not special for JWT/OAuth there is no lab in the workshop for this.

<hr>

This is the end of the lab. In the next [lab 3](../lab3) we will propagate the JWT to call another Microservice.
