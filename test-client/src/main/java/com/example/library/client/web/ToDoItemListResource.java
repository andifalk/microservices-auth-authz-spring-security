package com.example.library.client.web;

public class ToDoItemListResource {

  private EmbeddedToDoItemListResource _embedded;

  public ToDoItemListResource() {}

  public ToDoItemListResource(EmbeddedToDoItemListResource embeddedToDoItemListResource) {
    this._embedded = embeddedToDoItemListResource;
  }

  public EmbeddedToDoItemListResource get_embedded() {
    return _embedded;
  }

  public void set_embedded(EmbeddedToDoItemListResource _embedded) {
    this._embedded = _embedded;
  }
}
