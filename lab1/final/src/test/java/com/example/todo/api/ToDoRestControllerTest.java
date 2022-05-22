package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.ToDoService;
import com.example.todo.service.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@WebMvcTest(ToDoRestController.class)
class ToDoRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ToDoService toDoService;

    @Test
    void findAllForUser() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", Set.of("USER"));
        UUID todoItemIdentifier = UUID.randomUUID();
        when(toDoService.findAllForUser(any(), any()))
                .thenReturn(List.of(new ToDoItem(todoItemIdentifier, "mytodo",
                        "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID))));
        this.mvc.perform(
                get("/api/todos")
                        .param("user", DataInitializer.WAYNE_ID)
                        .with(user(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                "\"description\":\"todo description\"," +
                                "\"userIdentifier\":\"" + user.getIdentifier() + "\"}]"
                ));
    }

    @Test
    void findAllForUserUnauthorized() throws Exception {
        this.mvc.perform(
                        get("/api/todos")
                                .param("user", DataInitializer.WAYNE_ID))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findOneForUser() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", Set.of("USER"));
        UUID todoItemIdentifier = UUID.randomUUID();
        when(toDoService.findToDoItemForUser(any(), any()))
                .thenReturn(Optional.of(new ToDoItem(todoItemIdentifier, "mytodo",
                        "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID))));
        this.mvc.perform(
                        get("/api/todos/{todoidentifier}", todoItemIdentifier.toString())
                                .with(user(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                "\"description\":\"todo description\"," +
                                "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                ));
    }

    @Test
    void findOneForUserUnauthorized() throws Exception {
        this.mvc.perform(
                        get("/api/todos/{todoidentifier}", UUID.randomUUID().toString()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

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
        this.mvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(item))
                                .with(user(user)).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"identifier\":\"" + todoItemIdentifier + "\",\"title\":\"mytodo\"," +
                                "\"description\":\"todo description\"," +
                                "\"userIdentifier\":\"" + user.getIdentifier() + "\"}"
                ));
    }

    @Test
    void createToDoItemUnauthorized() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", Set.of("USER"));
        UUID todoItemIdentifier = UUID.randomUUID();
        ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                "todo description", null, UUID.fromString(DataInitializer.WAYNE_ID));
        this.mvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(item))
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}