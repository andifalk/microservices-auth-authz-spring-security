package com.example.todo.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class ToDoItemEntity extends AbstractPersistable<Long> {

    // Required fix for H2 UUID column in spring boot 2.7.x
    @Column(length=16)
    @NotNull
    private UUID identifier;

    @Size(min = 1, max = 30)
    private String title;

    @Size(max = 100)
    private String description;

    private LocalDate dueDate;

    @NotNull
    @Column(length=16)
    private UUID userIdentifier;

    public ToDoItemEntity() {
    }

    public ToDoItemEntity(UUID identifier, String title, String description, LocalDate dueDate, UUID userIdentifier) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.userIdentifier = userIdentifier;
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

    public UUID getUserIdentifier() {
        return userIdentifier;
    }

    @Override
    public String toString() {
        return "ToDoEntity{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", userIdentifier=" + userIdentifier +
                "} " + super.toString();
    }
}
