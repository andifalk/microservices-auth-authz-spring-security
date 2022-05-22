package com.example.library.client.web;

import java.time.LocalDate;
import java.util.UUID;

public class ToDoItemResource {

  private UUID identifier;

  private String title;

  private String description;

  private LocalDate dueDate;

  private UUID userIdentifier;

  @SuppressWarnings("unused")
  public ToDoItemResource() {}

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public UUID getUserIdentifier() {
    return userIdentifier;
  }
  public void setUserIdentifier(UUID userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  @Override
  public String toString() {
    return "ToDoItemResource{" +
            "identifier=" + identifier +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", dueDate=" + dueDate +
            ", userIdentifier=" + userIdentifier +
            '}';
  }
}
