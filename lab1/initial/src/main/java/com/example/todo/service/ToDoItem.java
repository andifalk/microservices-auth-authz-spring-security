package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ToDoItem implements Serializable {
    private UUID identifier;

    @Size(min = 1, max = 30)
    private String title;

    @Size(max = 100)
    private String description;

    private LocalDate dueDate;

    private User user;

    public ToDoItem() {
    }

    public ToDoItem(UUID identifier, String title, String description, LocalDate dueDate, User user) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.user = user;
    }

    public ToDoItem(ToDoItemEntity toDoItemEntity) {
        this.identifier = toDoItemEntity.getIdentifier();
        this.description = toDoItemEntity.getDescription();
        this.dueDate = toDoItemEntity.getDueDate();
        this.title = toDoItemEntity.getTitle();
        this.user = new User(toDoItemEntity.getUserEntity());
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ToDoItemEntity toTodoItemEntity() {
        return new ToDoItemEntity(this.identifier, this.title, this.description, this.dueDate, this.user.toUserEntity());
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", user=" + user +
                "} " + super.toString();
    }
}
