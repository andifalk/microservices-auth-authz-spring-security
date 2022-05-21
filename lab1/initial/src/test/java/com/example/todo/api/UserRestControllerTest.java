package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.CreateUser;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.User;
import com.example.todo.service.UserService;
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
@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void findAllUsers() throws Exception {
        User pParker = getPeterParker();
        User bWayne = getBruceWayne();
        when(userService.findAll())
                .thenReturn(List.of(pParker, bWayne));
        this.mvc.perform(
                get("/api/users")
                        .with(user(pParker)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"identifier\":\"" + pParker.getIdentifier() + "\",\"firstName\":\"Peter\"," +
                                "\"lastName\":\"Parker\",\"username\":\"pparker\",\"email\":\"peter.parker@example.com\"}," +
                                "{\"identifier\":\"" + bWayne.getIdentifier() + "\",\"firstName\":\"Bruce\"," +
                                "\"lastName\":\"Wayne\",\"username\":\"bwayne\",\"email\":\"bruce.wayne@example.com\"}]"
                ));
    }

    @Test
    void findAllUsersUnauthorized() throws Exception {
        this.mvc.perform(
                        get("/api/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findOneForUser() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", "wayne", Set.of("USER"));
        when(userService.findOneByIdentifier(any()))
                .thenReturn(Optional.of(user));
        this.mvc.perform(
                        get("/api/users/{useridentifier}", user.getIdentifier().toString())
                                .with(user(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"identifier\":\"" + user.getIdentifier() + "\",\"firstName\":\"Bruce\"," +
                                "\"lastName\":\"Wayne\",\"username\":\"bwayne\",\"email\":\"bruce.wayne@example.com\"}"
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
    void getAuthenticatedUser() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", "wayne", Set.of("USER"));

        this.mvc.perform(
                        get("/api/users/me")
                                .with(user(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"identifier\":\"" + user.getIdentifier() + "\",\"firstName\":\"Bruce\"," +
                                "\"lastName\":\"Wayne\",\"username\":\"bwayne\",\"email\":\"bruce.wayne@example.com\"}"
                ));
    }

    @Test
    void createUser() throws Exception {
        CreateUser createUser = new CreateUser(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", "wayne", Set.of("USER"));
        User user = getPeterParker();
        UUID todoItemIdentifier = UUID.randomUUID();
        ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                "todo description", null, user);
        when(userService.create(any()))
                .thenReturn(new User(
                        UUID.fromString(DataInitializer.WAYNE_ID),
                        "Bruce", "Wayne",
                        "bwayne", "bruce.wayne@example.com", "wayne", Set.of("USER")));
        this.mvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(item))
                                .with(user(user)).with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(
                        "{\"identifier\":\"" + createUser.getIdentifier() + "\",\"firstName\":\"Bruce\"," +
                                "\"lastName\":\"Wayne\",\"username\":\"bwayne\",\"email\":\"bruce.wayne@example.com\"}"
                ));
    }

    @Test
    void createToDoItemUnauthorized() throws Exception {
        User user = getBruceWayne();
        UUID todoItemIdentifier = UUID.randomUUID();
        ToDoItem item = new ToDoItem(todoItemIdentifier, "mytodo",
                "todo description", null, user);
        this.mvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(item))
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private User getBruceWayne() {
        return new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "bruce.wayne@example.com", "wayne", Set.of("USER"));
    }

    private User getPeterParker() {
        return new User(
                UUID.fromString(DataInitializer.PARKER_ID),
                "Peter", "Parker",
                "pparker", "peter.parker@example.com", "parker", Set.of("USER", "ADMIN"));
    }
}