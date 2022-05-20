package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.ToDoService;
import com.example.todo.service.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ToDoRestController.class)
class ToDoRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ToDoService toDoService;

    @WithMockUser(roles = "USER")
    @Test
    void findAllForUser() throws Exception {
        User user = new User(
                UUID.fromString(DataInitializer.WAYNE_ID),
                "Bruce", "Wayne",
                "bwayne", "wayne", Set.of("USER"));
        when(toDoService.findAllForUser(any(), any()))
                .thenReturn(List.of(new ToDoItem(UUID.randomUUID(), "mytodo", "todo description", null, user)));
        this.mvc.perform(
                get("/api/todos")
                        .param("user", DataInitializer.WAYNE_ID)
                        .with(user(user)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json("[{\"identifier\":\"" + user.getIdentifier() + "\",\"title\":\"mytodo\",\"description\":\"todo description\",\"dueDate\":\"\",\"user\":{\"identifier\":\"c52bf7db-db55-4f89-ac53-82b40e8c57c2\",\"firstName\":\"Bruce\",\"lastName\":\"Wayne\",\"username\":\"bwayne\"}}]"));
    }

    @Test
    void findOneForUser() {
    }

    @Test
    void create() {
    }
}