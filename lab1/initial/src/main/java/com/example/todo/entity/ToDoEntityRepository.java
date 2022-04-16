package com.example.todo.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ToDoEntityRepository extends JpaRepository<ToDoEntity, Long> {
}
