package com.example.todo.service;

import com.example.todo.entity.ToDoEntity;
import com.example.todo.entity.ToDoEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ToDoService {

    private final ToDoEntityRepository toDoEntityRepository;

    public ToDoService(ToDoEntityRepository toDoEntityRepository) {
        this.toDoEntityRepository = toDoEntityRepository;
    }

    public List<ToDoEntity> findAll() {
        return toDoEntityRepository.findAll();
    }

    public <S extends ToDoEntity> S save(S entity) {
        return toDoEntityRepository.save(entity);
    }
}
