package com.example.todo.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
public class ToDoItemEntity extends AbstractPersistable<Long> {
    @Column(length=16)
    @NotNull
    private UUID identifier;

    @Size(min = 1, max = 30)
    private String title;

    @Size(max = 30)
    private String description;

    private LocalDate dueDate;

    @NotNull
    @ManyToOne(fetch = EAGER, cascade = ALL)
    private UserEntity userEntity;

    public ToDoItemEntity() {
    }

    public ToDoItemEntity(UUID identifier, String title, String description, LocalDate dueDate, UserEntity userEntity) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.userEntity = userEntity;
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

    public UserEntity getUserEntity() {
        return userEntity;
    }

    @Override
    public String toString() {
        return "ToDoEntity{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", userEntity=" + userEntity +
                "} " + super.toString();
    }
}
