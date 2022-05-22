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

    private UUID userIdentifier;

    public ToDoItem() {
    }

    public ToDoItem(UUID identifier, String title, String description, LocalDate dueDate, UUID userIdentifier) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.userIdentifier = userIdentifier;
    }

    public ToDoItem(ToDoItemEntity toDoItemEntity) {
        this.identifier = toDoItemEntity.getIdentifier();
        this.description = toDoItemEntity.getDescription();
        this.dueDate = toDoItemEntity.getDueDate();
        this.title = toDoItemEntity.getTitle();
        this.userIdentifier = toDoItemEntity.getUserIdentifier();
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

    public UUID getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(UUID userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public ToDoItemEntity toTodoItemEntity() {
        return new ToDoItemEntity(this.identifier, this.title, this.description, this.dueDate, this.userIdentifier);
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", userIdentifier=" + userIdentifier +
                "} " + super.toString();
    }
}
