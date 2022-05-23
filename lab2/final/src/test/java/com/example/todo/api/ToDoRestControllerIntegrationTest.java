package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.entity.ToDoItemEntity;
import com.example.todo.entity.ToDoItemEntityRepository;
import com.example.todo.service.ToDoItem;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
                .apply(springSecurity())
                .build();
    }

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

            Jwt jwt = getJwt(DataInitializer.PARKER_ID, List.of("USER", "ADMIN"));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.PARKER_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
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

            Jwt jwt = getJwt(DataInitializer.WAYNE_ID, List.of("USER"));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
            verify(toDoItemEntityRepository, never()).findAll();
            verify(toDoItemEntityRepository).findAllByUserIdentifier(any());
        }
    }

    @DisplayName("Retrieving all ToDos for a user")
    @Nested
    class FindAllToDosForUser {
        @DisplayName("is successful")
        @Test
        void findAllToDosForUser() throws Exception {
            User user = getBruceWayne();
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.findAllByUserIdentifier(any())).thenReturn(List.of(toDoItem));

            Jwt jwt = getJwt(DataInitializer.WAYNE_ID, List.of("USER"));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.WAYNE_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
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

            Jwt jwt = getJwt(DataInitializer.WAYNE_ID, List.of("USER"));

            mvc.perform(
                            get("/api/todos")
                                    .param("user", DataInitializer.KENT_ID)
                                    .with(jwt().jwt(jwt).authorities(new JwtAuthzConverter())))
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
            Jwt jwt = getJwt(DataInitializer.WAYNE_ID, List.of("INVALID"));

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
            User user = getBruceWayne();
            ToDoItemEntity toDoItem = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.findOneByIdentifierAndUserIdentifier(any(), any()))
                    .thenReturn(Optional.of(toDoItem));
            mvc.perform(
                            get("/api/todos/{todoIdentifier}", toDoItem.getIdentifier().toString())
                                    .with(jwt()
                                            .jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                            .claim("roles", List.of("USER"))).authorities(new JwtAuthzConverter())))
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
                            get("/api/todos/{todoIdentifier}", toDoItem.getIdentifier().toString())
                                    .with(jwt()
                                            .jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                                    .claim("roles", List.of("USER"))).authorities(new JwtAuthzConverter())))
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
                            get("/api/todos/{todoIdentifier}", UUID.randomUUID().toString()).with(jwt()
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
            User user = getBruceWayne();
            ToDoItem toDoItem = getToDoItem(UUID.fromString(DataInitializer.WAYNE_ID));
            ToDoItemEntity toDoItemEntity = getToDoItemEntity(UUID.fromString(DataInitializer.WAYNE_ID));
            when(toDoItemEntityRepository.save(any(ToDoItemEntity.class)))
                    .thenReturn(toDoItemEntity);
            mvc.perform(
                            post("/api/todos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(toDoItem))
                                    .with(jwt().jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                                    .claim("roles", List.of("USER")))
                                            .authorities(new JwtAuthzConverter())))
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
                                    .content(objectMapper.writeValueAsString(toDoItem))
                                    .with(jwt().jwt(jwt -> jwt.subject(DataInitializer.WAYNE_ID)
                                                    .claim("roles", List.of("INVALID")))
                                                    .authorities(new JwtAuthzConverter())))
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

    private Jwt getJwt(String userIdentifier, List<String> roles) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userIdentifier)
                .claim("roles", roles)
                .build();
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