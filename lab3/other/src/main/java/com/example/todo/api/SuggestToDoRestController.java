package com.example.todo.api;

import com.example.todo.service.SuggestToDoService;
import com.example.todo.service.SuggestedToDoItem;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suggest")
@Validated
@OpenAPIDefinition(tags = @Tag(name = "todo"), info = @Info(title = "Suggest ToDo", description = "API to suggest ToDo Items", version = "1"), security = {@SecurityRequirement(name = "bearer")})
public class SuggestToDoRestController {

    private final SuggestToDoService suggestToDoService;

    public SuggestToDoRestController(SuggestToDoService suggestToDoService) {
        this.suggestToDoService = suggestToDoService;
    }

    @Operation(tags = "todo", summary = "Suggest ToDo API", description = "Suggest a possible ToDo item")
    @GetMapping
    public SuggestedToDoItem suggestedToDoItem() {
        return suggestToDoService.suggestToDoItem();
    }

}
