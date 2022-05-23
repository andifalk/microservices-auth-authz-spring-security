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

Open the existing class _ToDoRestControllerIntegrationTest_ and add the missing parts.

```java
package com.example.library.server.api;

import com.example.library.server.DataInitializer;
import com.example.library.server.api.resource.BookResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@DisplayName("Verify book api")
class BookApiJwtAuthorizationTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
  }

  @DisplayName("can authorize to")
  @Nested
  class CanAuthorize {

    @Test
    @DisplayName("get list of books")
    void verifyGetBooks() throws Exception {

      mockMvc.perform(get("/books").with(jwt())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("get single book")
    void verifyGetBook() throws Exception {

      Jwt jwt =
          Jwt.withTokenValue("token")
              .header("alg", "none")
              .claim("sub", "bwayne")
              .claim("groups", new String[] {"library_user"})
              .build();

      mockMvc
          .perform(
              get("/books/{bookId}", DataInitializer.BOOK_CLEAN_CODE_IDENTIFIER).with(jwt(jwt)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("delete a book")
    void verifyDeleteBook() throws Exception {
      mockMvc
          .perform(
              delete("/books/{bookId}", DataInitializer.BOOK_DEVOPS_IDENTIFIER)
                  .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LIBRARY_CURATOR"))))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("create a new book")
    void verifyCreateBook() throws Exception {

      BookResource bookResource =
          new BookResource(
              UUID.randomUUID(),
              "1234566",
              "title",
              "description",
              Collections.singletonList("Author"),
              false,
              null);

      mockMvc
          .perform(
              post("/books")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bookResource))
                  .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LIBRARY_CURATOR"))))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("update a book")
    void verifyUpdateBook() throws Exception {

      BookResource bookResource =
          new BookResource(
              DataInitializer.BOOK_SPRING_ACTION_IDENTIFIER,
              "9781617291203",
              "Spring in Action: Covers Spring 5",
              "Spring in Action, Fifth Edition is a hands-on guide to the Spring Framework, "
                  + "updated for version 4. It covers the latest features, tools, and practices "
                  + "including Spring MVC, REST, Security, Web Flow, and more. You'll move between "
                  + "short snippets and an ongoing example as you learn to build simple and efficient "
                  + "J2EE applications. Author Craig Walls has a special knack for crisp and "
                  + "entertaining examples that zoom in on the features and techniques you really need.",
              Collections.singletonList("Craig Walls"),
              false,
              null);

      mockMvc
          .perform(
              put("/books/{bookId}", DataInitializer.BOOK_SPRING_ACTION_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bookResource))
                  .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LIBRARY_CURATOR"))))
          .andExpect(status().isOk());
    }
  }

  @DisplayName("cannot authorize to")
  @Nested
  class CannotAuthorize {

    @Test
    @DisplayName("get list of books")
    void verifyGetBooksUnAuthenticated() throws Exception {

      mockMvc.perform(get("/books")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("get single book")
    void verifyGetBook() throws Exception {

      mockMvc
              .perform(
                      get("/books/{bookId}",
                              DataInitializer.BOOK_CLEAN_CODE_IDENTIFIER))
              .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("delete a book")
    void verifyDeleteBookUnAuthorized() throws Exception {
      mockMvc
              .perform(
                      delete("/books/{bookId}", DataInitializer.BOOK_DEVOPS_IDENTIFIER))
              .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("delete a book with wrong role")
    void verifyDeleteBookWrongRole() throws Exception {
      mockMvc
              .perform(
                      delete("/books/{bookId}", DataInitializer.BOOK_DEVOPS_IDENTIFIER)
                              .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LIBRARY_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("create a new book")
    void verifyCreateBookUnAuthorized() throws Exception {

      BookResource bookResource =
              new BookResource(
                      UUID.randomUUID(),
                      "1234566",
                      "title",
                      "description",
                      Collections.singletonList("Author"),
                      false,
                      null);

      mockMvc
              .perform(
                      post("/books")
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(objectMapper.writeValueAsString(bookResource)))
              .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("update a book")
    void verifyUpdateBookUnAuthorized() throws Exception {

      BookResource bookResource =
              new BookResource(
                      DataInitializer.BOOK_SPRING_ACTION_IDENTIFIER,
                      "9781617291203",
                      "Spring in Action: Covers Spring 5",
                      "Spring in Action, Fifth Edition is a hands-on guide to the Spring Framework, "
                              + "updated for version 4. It covers the latest features, tools, and practices "
                              + "including Spring MVC, REST, Security, Web Flow, and more. You'll move between "
                              + "short snippets and an ongoing example as you learn to build simple and efficient "
                              + "J2EE applications. Author Craig Walls has a special knack for crisp and "
                              + "entertaining examples that zoom in on the features and techniques you really need.",
                      Collections.singletonList("Craig Walls"),
                      false,
                      null);

      mockMvc
              .perform(
                      put("/books/{bookId}", DataInitializer.BOOK_SPRING_ACTION_IDENTIFIER)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(objectMapper.writeValueAsString(bookResource)))
              .andExpect(status().isUnauthorized());
    }
  }
}
```

Please also have a look at the other tests as well in the reference solution.

<hr>

This is the end of the lab. In the next [lab 3](../lab3) we will propagate the JWT to call another Microservice.

<hr>

To continue with the JWT testing server please continue at [Lab 3](../lab3).