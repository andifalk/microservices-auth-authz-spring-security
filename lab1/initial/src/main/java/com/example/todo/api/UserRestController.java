package com.example.todo.api;

import com.example.todo.service.User;
import com.example.todo.service.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/users")
@OpenAPIDefinition(tags = @Tag(name = "user"), info = @Info(title = "User", description = "API for Users", version = "1"), security = {@SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "bearer")})
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @Operation(tags = "user", summary = "User API", description = "Finds all registered users")
    @GetMapping
    public List<User> allUsers() {
        return userService.findAll();
    }

    @Operation(tags = "user", summary = "User API", description = "Finds user specified by user identifier")
    @GetMapping("/{userIdentifier}")
    public ResponseEntity<User> findUser(@PathVariable UUID userIdentifier) {
        return userService.findOneByIdentifier(userIdentifier).map(
                ResponseEntity::ok
        ).orElse(ResponseEntity.notFound().build());
    }

    @Operation(tags = "user", summary = "User API", description = "Retrieves the currently authenticated user")
    @GetMapping("/me")
    public User getAuthenticatedUser(@AuthenticationPrincipal User authenticatedUser) {
        return authenticatedUser;
    }

    @Operation(tags = "user", summary = "User API", description = "Creates a new user")
    @PostMapping
    @ResponseStatus(CREATED)
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }

}
