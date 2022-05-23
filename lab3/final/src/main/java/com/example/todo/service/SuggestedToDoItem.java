package com.example.todo.service;

import java.io.Serializable;

public class SuggestedToDoItem implements Serializable {

    private String title;

    private String description;

    public SuggestedToDoItem() {
    }

    public SuggestedToDoItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}
