package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.ToDoService;
import com.example.todo.service.User;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@OpenAPIDefinition(tags = @Tag(name = "todo"), info = @Info(title = "ToDo", description = "API for ToDo Items", version = "1"), security = {@SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "bearer")})
public class ToDoRestController {

    private final ToDoService toDoService;

    @Operation(tags = "todo", summary = "ToDo API", description = "Finds all ToDo items for given user identifier", parameters = @Parameter(name = "user", example = DataInitializer.WAYNE_ID))
    @GetMapping
    public List<ToDoItem> findAllForUser(@RequestParam(name = "user") UUID userIdentifier, @AuthenticationPrincipal User authenticatedUser) {
        if (authenticatedUser.isAdmin()) {
            return toDoService.findAll();
        } else {
            return toDoService.findAllForUser(userIdentifier, authenticatedUser);
        }
    }

    @Operation(tags = "todo", summary = "ToDo API", description = "Finds one ToDo item for given todo item identifier")
    @GetMapping("/{todoItemIdentifier}")
    public ResponseEntity<ToDoItem> findOneForUser(
            @PathVariable("todoItemIdentifier") UUID todoItemIdentifier,
            @AuthenticationPrincipal User authenticatedUser) {
        return toDoService.findToDoItemForUser(todoItemIdentifier, authenticatedUser)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(tags = "todo", summary = "ToDo API", description = "Creates a new ToDo item for current user")
    @PostMapping
    public ToDoItem create(@RequestBody ToDoItem toDoItem, @AuthenticationPrincipal User authenticatedUser) {
        toDoItem.setUser(authenticatedUser);
        return toDoService.save(toDoItem);
    }

    public ToDoRestController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }
}
