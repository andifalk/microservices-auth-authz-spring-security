package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntityRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Transactional(readOnly = true)
public class ToDoService {

    private final ToDoItemEntityRepository toDoItemEntityRepository;

    public ToDoService(ToDoItemEntityRepository toDoItemEntityRepository) {
        this.toDoItemEntityRepository = toDoItemEntityRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ToDoItem> findAll() {
        return toDoItemEntityRepository.findAll()
                .stream().map(ToDoItem::new).collect(Collectors.toList());
    }

    public List<ToDoItem> findAllForUser(UUID userIdentifier, User authenticatedUser) {
        if (!authenticatedUser.getIdentifier().equals(userIdentifier)) {
            throw new AccessDeniedException("Current user is not allowed to access todo items for given user id");
        }
        return toDoItemEntityRepository.findAllByUserEntityIdentifier(userIdentifier)
                .stream().map(ToDoItem::new).collect(Collectors.toList());
    }

    public Optional<ToDoItem> findToDoItemForUser(UUID toDoItemIdentifier, User authenticatedUser) {
        return toDoItemEntityRepository.findOneByIdentifierAndUserEntityIdentifier(
                toDoItemIdentifier, authenticatedUser.getIdentifier()).map(ToDoItem::new);
    }

    @Transactional
    public ToDoItem create(ToDoItem toDoItem) {
        if (toDoItem.getIdentifier() == null) {
            toDoItem.setIdentifier(UUID.randomUUID());
        }
        return new ToDoItem(toDoItemEntityRepository.save(toDoItem.toTodoItemEntity()));
    }
}
