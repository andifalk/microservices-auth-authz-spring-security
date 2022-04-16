package com.example.todo.service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ToDo implements Serializable {
    private UUID identifier;

    @Size(min = 1, max = 30)
    private String title;

    @Size(max = 30)
    private String description;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private User user;

    public ToDo() {
    }

    public ToDo(UUID identifier, String title, String description, LocalDate dueDate, User user) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.user = user;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public User getUserEntity() {
        return user;
    }

    @Override
    public String toString() {
        return "ToDo{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", userEntity=" + user +
                "} " + super.toString();
    }
}
