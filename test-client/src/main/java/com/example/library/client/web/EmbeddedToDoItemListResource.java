package com.example.library.client.web;

import java.util.Collection;

public class EmbeddedToDoItemListResource {

  private Collection<ToDoItemResource> toDoItemResourceList;

  public EmbeddedToDoItemListResource() {}

  public EmbeddedToDoItemListResource(Collection<ToDoItemResource> toDoItems) {
    this.toDoItemResourceList = toDoItems;
  }

  public Collection<ToDoItemResource> getToDoItemResourceList() {
    return toDoItemResourceList;
  }

  public void setToDoItemResourceList(Collection<ToDoItemResource> toDoItems) {
    this.toDoItemResourceList = toDoItems;
  }
}
