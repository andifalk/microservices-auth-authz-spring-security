package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.ToDoService;
import com.example.todo.service.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToDoRestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ToDoService toDoService;

    @MockBean
    private H2ConsoleProperties h2ConsoleProperties;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Retrieving all ToDos for a user")
    @Nested
    class FindAllToDos {
        @DisplayName("is successful")
        @Test
        void findAllToDosForUser() throws Exception {
            User user = new User(
                    UUID.fromString(DataInitializer.WAYNE_ID),
                    "Bruce", "Wayne",
                    "bwayne", "bruce.wayne@example.com", Set.of("USER"));
            UUID todoItemIdentifier = UUID.randomUUID();
            when(toDoService.findAllForUser(any(), any()))
                    .thenReturn(List.of(new ToDoItem(todoItemIdentifier, "mytodo",
                            "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID))));

            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", DataInitializer.WAYNE_ID)
                    .claim("roles", List.of("USER"))
                    .build();

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "[{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}]"
                    ));
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
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", DataInitializer.WAYNE_ID)
                    .claim("roles", List.of("INVALID"))
                    .build();
            mvc.perform(
                            get("/api/todos").with(jwt().jwt(jwt).authorities(new JwtAuthzConverter()))
                                    .param("user", DataInitializer.WAYNE_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @DisplayName("Retrieving one ToDo for a user")
    @Nested
    class FindOneToDoForUser {
        @DisplayName("is successful")
        @Test
        void findOneToDoForUser() throws Exception {
            User user = new User(
                    UUID.fromString(DataInitializer.WAYNE_ID),
                    "Bruce", "Wayne",
                    "bwayne", "bruce.wayne@example.com", Set.of("USER"));
            UUID todoItemIdentifier = UUID.randomUUID();
            when(toDoService.findToDoItemForUser(any(), any()))
                    .thenReturn(Optional.of(new ToDoItem(todoItemIdentifier, "mytodo",
                            "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID))));
            mvc.perform(
                            get("/api/todos/{todoidentifier}", todoItemIdentifier.toString())
                                    .with(jwt()
                                            .jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                            .claim("roles", List.of("USER"))).authorities(new JwtAuthzConverter())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                    ));
        }

        @DisplayName("fails when unauthenticated")
        @Test
        void findOneToDoForUserUnauthorized() throws Exception {
            mvc.perform(
                            get("/api/todos/{todoidentifier}", UUID.randomUUID().toString()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("fails when unauthorized")
        @Test
        void findOneToDoForUserForbidden() throws Exception {
            mvc.perform(
                            get("/api/todos/{todoidentifier}", UUID.randomUUID().toString()).with(jwt()
                                    .jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                    .claim("roles", List.of("INVALID")))
                                    .authorities(new JwtAuthzConverter())))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

    }

    @DisplayName("Creating a new ToDo")
    @Nested
    class CreateToDo {
        @DisplayName("is successful")
        @Test
        void createToDoItem() throws Exception {
            User user = new User(
                    UUID.fromString(DataInitializer.WAYNE_ID),
                    "Bruce", "Wayne",
                    "bwayne", "bruce.wayne@example.com", Set.of("USER"));
            UUID todoItemIdentifier = UUID.randomUUID();
            ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                    "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoService.create(any()))
                    .thenReturn(new ToDoItem(todoItemIdentifier, "mytodo",
                            "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID)));
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(item))
                                    .with(jwt().jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                                    .claim("roles", List.of("USER")))
                                            .authorities(new JwtAuthzConverter())).with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                    "\"description\":\"todo description\"," +
                                    "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                    ));
        }

        @DisplayName("fails when unauthenticated")
        @Test
        void createToDoItemUnauthorized() throws Exception {
            UUID todoItemIdentifier = UUID.randomUUID();
            ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                    "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID));
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(item))
                                    .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("fails when unauthorized")
        @Test
        void createToDoItemForbidden() throws Exception {
            UUID todoItemIdentifier = UUID.randomUUID();
            ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                    "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID));
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(item))
                                    .with(jwt().jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                                    .claim("roles", List.of("INVALID")))
                                                    .authorities(new JwtAuthzConverter()))
                                    .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }


    static class JwtAuthzConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
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